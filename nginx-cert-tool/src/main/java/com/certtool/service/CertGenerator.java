package com.certtool.service;

import com.certtool.model.CaConfig;
import com.certtool.model.CertResult;
import com.certtool.model.ServerCertConfig;
import com.certtool.model.ServerCertGenerationResult;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertGenerator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CertResult generateRootCa(CaConfig config, String outputDir) throws Exception {
        KeyPair keyPair = generateKeyPair(config.getKeySize());
        X500Name subject = buildCaSubject(config);

        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + (long) config.getValidityYears() * 365 * 24 * 3600 * 1000L);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, keyPair.getPublic());

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(keyPair.getPublic()));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(keyPair.getPrivate());

        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certBuilder.build(signer));

        String certPem = toPem("CERTIFICATE", cert.getEncoded());
        String keyPem = toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded());

        String certPath = outputDir + "/ca.crt";
        String keyPath = outputDir + "/ca.key";
        CertExporter.writePem(certPath, certPem);
        CertExporter.writePem(keyPath, keyPem);

        return new CertResult(certPem, keyPem, certPath, keyPath);
    }

    public CertResult generateServerCert(ServerCertConfig config, String outputDir) throws Exception {
        ServerCertCore core = generateServerCertCore(config);

        String certPem = toPem("CERTIFICATE", core.cert.getEncoded());
        String keyPem = toPem("PRIVATE KEY", core.keyPair.getPrivate().getEncoded());

        String certPath = outputDir + "/" + config.getCommonName() + ".crt";
        String keyPath = outputDir + "/" + config.getCommonName() + ".key";
        CertExporter.writePem(certPath, certPem);
        CertExporter.writePem(keyPath, keyPem);

        return new CertResult(certPem, keyPem, certPath, keyPath);
    }

    /**
     * Generate a PKCS12 keystore for Tomcat.
     * Bundles the server cert, private key, and CA cert chain into a .p12 file.
     */
    public CertResult generateTomcatKeystore(ServerCertConfig config) throws Exception {
        ServerCertCore core = generateServerCertCore(config);

        // Load CA cert for the chain
        X509Certificate caCert = loadCertFromPem(config.getCaCertPath());

        // Build PKCS12 keystore with server cert + CA chain
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(
                config.getCommonName(),
                core.keyPair.getPrivate(),
                config.getTomcatKeystorePassword().toCharArray(),
                new X509Certificate[]{core.cert, caCert}
        );

        String outputPath = config.getTomcatOutputDir() + "/" + config.getCommonName() + ".p12";
        CertExporter.writePkcs12(outputPath, ks, config.getTomcatKeystorePassword());

        String certPem = toPem("CERTIFICATE", core.cert.getEncoded());
        String keyPem = toPem("PRIVATE KEY", core.keyPair.getPrivate().getEncoded());

        return new CertResult(certPem, keyPem, outputPath, outputPath);
    }

    /**
     * Generate server certificates for all selected types (Nginx and/or Tomcat).
     *
     * @return aggregated result; each sub-result is null if the type was not selected
     */
    public ServerCertGenerationResult generateServerCerts(ServerCertConfig config) throws Exception {
        CertResult nginxResult = null;
        CertResult tomcatResult = null;

        if (config.isNginxEnabled()) {
            nginxResult = generateServerCert(config, config.getNginxOutputDir());
        }
        if (config.isTomcatEnabled()) {
            tomcatResult = generateTomcatKeystore(config);
        }

        return new ServerCertGenerationResult(nginxResult, tomcatResult);
    }

    /**
     * Core: generate an X.509 server certificate and key pair.
     * Shared by both Nginx (PEM) and Tomcat (PKCS12) paths.
     */
    private ServerCertCore generateServerCertCore(ServerCertConfig config) throws Exception {
        X509Certificate caCert = loadCertFromPem(config.getCaCertPath());
        PrivateKey caKey = (PrivateKey) loadKeyFromPem(config.getCaKeyPath());

        KeyPair serverKeyPair = generateKeyPair(config.getKeySize());
        X500Name subject = new X500Name("CN=" + config.getCommonName());

        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + (long) config.getValidityDays() * 24 * 3600 * 1000L);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                caCert, serial, notBefore, notAfter, subject, serverKeyPair.getPublic());

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
        certBuilder.addExtension(Extension.keyUsage, true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        certBuilder.addExtension(Extension.extendedKeyUsage, false,
                new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));

        org.bouncycastle.asn1.x509.GeneralNames sanNames = buildSanNames(config);
        if (sanNames != null) {
            certBuilder.addExtension(Extension.subjectAlternativeName, false, sanNames);
        }

        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caCert.getPublicKey()));
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(serverKeyPair.getPublic()));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(caKey);

        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certBuilder.build(signer));

        return new ServerCertCore(cert, serverKeyPair);
    }

    /** Holder for the generated server certificate and key pair. */
    private static class ServerCertCore {
        final X509Certificate cert;
        final KeyPair keyPair;

        ServerCertCore(X509Certificate cert, KeyPair keyPair) {
            this.cert = cert;
            this.keyPair = keyPair;
        }
    }

    private KeyPair generateKeyPair(int keySize) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(keySize);
        return gen.generateKeyPair();
    }

    private X500Name buildCaSubject(CaConfig config) {
        StringBuilder dn = new StringBuilder();
        dn.append("CN=").append(config.getCommonName());
        if (config.getOrganization() != null && !config.getOrganization().isBlank()) {
            dn.append(",O=").append(config.getOrganization());
        }
        if (config.getOrganizationalUnit() != null && !config.getOrganizationalUnit().isBlank()) {
            dn.append(",OU=").append(config.getOrganizationalUnit());
        }
        return new X500Name(dn.toString());
    }

    private org.bouncycastle.asn1.x509.GeneralNames buildSanNames(ServerCertConfig config) {
        java.util.List<org.bouncycastle.asn1.x509.GeneralName> names = new java.util.ArrayList<>();
        for (String dns : config.getSanDns()) {
            if (!dns.isBlank()) {
                names.add(new org.bouncycastle.asn1.x509.GeneralName(
                        org.bouncycastle.asn1.x509.GeneralName.dNSName, dns.trim()));
            }
        }
        for (String ip : config.getSanIp()) {
            if (!ip.isBlank()) {
                names.add(new org.bouncycastle.asn1.x509.GeneralName(
                        org.bouncycastle.asn1.x509.GeneralName.iPAddress, ip.trim()));
            }
        }
        if (names.isEmpty()) return null;
        return new org.bouncycastle.asn1.x509.GeneralNames(
                names.toArray(new org.bouncycastle.asn1.x509.GeneralName[0]));
    }

    private X509Certificate loadCertFromPem(String path) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(path))) {
            Object obj = parser.readObject();
            if (obj instanceof X509CertificateHolder holder) {
                return new JcaX509CertificateConverter()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .getCertificate(holder);
            }
            throw new IllegalArgumentException("Invalid CA certificate file: " + path);
        }
    }

    private Object loadKeyFromPem(String path) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(path))) {
            Object obj = parser.readObject();
            if (obj instanceof org.bouncycastle.openssl.PEMKeyPair keyPair) {
                return new JcaPEMKeyConverter()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .getPrivateKey(keyPair.getPrivateKeyInfo());
            }
            if (obj instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo pkInfo) {
                return new JcaPEMKeyConverter()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .getPrivateKey(pkInfo);
            }
            throw new IllegalArgumentException("Invalid CA key file: " + path);
        }
    }

    private String toPem(String type, byte[] encoded) throws Exception {
        StringWriter sw = new StringWriter();
        try (PemWriter pw = new PemWriter(sw)) {
            pw.writeObject(new PemObject(type, encoded));
        }
        return sw.toString();
    }
}
