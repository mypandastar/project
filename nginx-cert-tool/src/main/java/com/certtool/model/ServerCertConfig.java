package com.certtool.model;

import java.util.ArrayList;
import java.util.List;

public class ServerCertConfig {

    private String commonName;
    private List<String> sanDns = new ArrayList<>();
    private List<String> sanIp = new ArrayList<>();
    private int validityDays = 365;
    private int keySize = 2048;
    private String caCertPath;
    private String caKeyPath;

    // --- v1.1: certificate type selection ---
    private boolean nginxEnabled = true;
    private String nginxOutputDir;
    private boolean tomcatEnabled = false;
    private String tomcatOutputDir;
    private String tomcatKeystorePassword;

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public List<String> getSanDns() { return sanDns; }
    public void setSanDns(List<String> sanDns) { this.sanDns = sanDns; }

    public List<String> getSanIp() { return sanIp; }
    public void setSanIp(List<String> sanIp) { this.sanIp = sanIp; }

    public int getValidityDays() { return validityDays; }
    public void setValidityDays(int validityDays) { this.validityDays = validityDays; }

    public int getKeySize() { return keySize; }
    public void setKeySize(int keySize) { this.keySize = keySize; }

    public String getCaCertPath() { return caCertPath; }
    public void setCaCertPath(String caCertPath) { this.caCertPath = caCertPath; }

    public String getCaKeyPath() { return caKeyPath; }
    public void setCaKeyPath(String caKeyPath) { this.caKeyPath = caKeyPath; }

    // --- v1.1 getters/setters ---
    public boolean isNginxEnabled() { return nginxEnabled; }
    public void setNginxEnabled(boolean nginxEnabled) { this.nginxEnabled = nginxEnabled; }

    public String getNginxOutputDir() { return nginxOutputDir; }
    public void setNginxOutputDir(String nginxOutputDir) { this.nginxOutputDir = nginxOutputDir; }

    public boolean isTomcatEnabled() { return tomcatEnabled; }
    public void setTomcatEnabled(boolean tomcatEnabled) { this.tomcatEnabled = tomcatEnabled; }

    public String getTomcatOutputDir() { return tomcatOutputDir; }
    public void setTomcatOutputDir(String tomcatOutputDir) { this.tomcatOutputDir = tomcatOutputDir; }

    public String getTomcatKeystorePassword() { return tomcatKeystorePassword; }
    public void setTomcatKeystorePassword(String tomcatKeystorePassword) { this.tomcatKeystorePassword = tomcatKeystorePassword; }
}
