package com.certtool.model;

public class CertResult {

    private final String certPem;
    private final String keyPem;
    private final String certPath;
    private final String keyPath;

    public CertResult(String certPem, String keyPem, String certPath, String keyPath) {
        this.certPem = certPem;
        this.keyPem = keyPem;
        this.certPath = certPath;
        this.keyPath = keyPath;
    }

    public String getCertPem() { return certPem; }
    public String getKeyPem() { return keyPem; }
    public String getCertPath() { return certPath; }
    public String getKeyPath() { return keyPath; }
}
