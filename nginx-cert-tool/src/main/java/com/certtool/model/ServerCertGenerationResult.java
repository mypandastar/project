package com.certtool.model;

/**
 * Aggregated result of server certificate generation.
 * Supports both Nginx (PEM) and Tomcat (PKCS12) output formats.
 * Each field is null if the corresponding type was not selected.
 */
public class ServerCertGenerationResult {

    private final CertResult nginxResult;
    private final CertResult tomcatResult;

    public ServerCertGenerationResult(CertResult nginxResult, CertResult tomcatResult) {
        this.nginxResult = nginxResult;
        this.tomcatResult = tomcatResult;
    }

    public CertResult getNginxResult() { return nginxResult; }
    public CertResult getTomcatResult() { return tomcatResult; }
}
