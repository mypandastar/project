package com.certtool.model;

public class CaConfig {

    private String commonName;
    private String organization;
    private String organizationalUnit;
    private int validityYears = 10;
    private int keySize = 2048;

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getOrganizationalUnit() { return organizationalUnit; }
    public void setOrganizationalUnit(String organizationalUnit) { this.organizationalUnit = organizationalUnit; }

    public int getValidityYears() { return validityYears; }
    public void setValidityYears(int validityYears) { this.validityYears = validityYears; }

    public int getKeySize() { return keySize; }
    public void setKeySize(int keySize) { this.keySize = keySize; }
}
