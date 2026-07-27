package com.certtool;

import com.certtool.model.CaConfig;
import com.certtool.model.CertResult;
import com.certtool.model.ServerCertConfig;
import com.certtool.service.CertGenerator;

import java.util.List;

/**
 * CLI test runner to verify cert generation end-to-end.
 * Run: java -cp target/classes com.certtool.TestRunner
 */
public class TestRunner {

    public static void main(String[] args) throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir") + "/cert-test";
        System.out.println("Output dir: " + tmpDir);

        CertGenerator generator = new CertGenerator();

        // Step 1: Generate Root CA
        System.out.println("\n=== Step 1: Generate Root CA ===");
        CaConfig caConfig = new CaConfig();
        caConfig.setCommonName("TestRootCA");
        caConfig.setOrganization("TestOrg");
        caConfig.setOrganizationalUnit("Dev");
        caConfig.setValidityYears(10);
        caConfig.setKeySize(2048);

        CertResult caResult = generator.generateRootCa(caConfig, tmpDir);
        System.out.println("CA Cert: " + caResult.getCertPath());
        System.out.println("CA Key:  " + caResult.getKeyPath());
        System.out.println("CA Cert PEM:\n" + caResult.getCertPem().substring(0, Math.min(200, caResult.getCertPem().length())) + "...");

        // Step 2: Generate Server Cert signed by CA
        System.out.println("\n=== Step 2: Generate Server Cert ===");
        ServerCertConfig serverConfig = new ServerCertConfig();
        serverConfig.setCommonName("test.local");
        serverConfig.setSanDns(List.of("*.test.local", "api.test.local"));
        serverConfig.setSanIp(List.of("127.0.0.1"));
        serverConfig.setValidityDays(365);
        serverConfig.setKeySize(2048);
        serverConfig.setCaCertPath(caResult.getCertPath());
        serverConfig.setCaKeyPath(caResult.getKeyPath());

        CertResult serverResult = generator.generateServerCert(serverConfig, tmpDir);
        System.out.println("Server Cert: " + serverResult.getCertPath());
        System.out.println("Server Key:  " + serverResult.getKeyPath());
        System.out.println("Server Cert PEM:\n" + serverResult.getCertPem().substring(0, Math.min(200, serverResult.getCertPem().length())) + "...");

        // Step 3: Verify cert chain via openssl if available
        System.out.println("\n=== Step 3: Verify ===");
        System.out.println("Generated files:");
        java.nio.file.Files.list(java.nio.file.Path.of(tmpDir)).forEach(p -> System.out.println("  " + p));

        System.out.println("\n=== ALL DONE ===");
        System.out.println("Verify manually with:");
        System.out.println("  openssl verify -CAfile \"" + caResult.getCertPath() + "\" \"" + serverResult.getCertPath() + "\"");
        System.out.println("  openssl x509 -in \"" + serverResult.getCertPath() + "\" -text -noout");
    }
}
