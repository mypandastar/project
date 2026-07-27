package com.certtool.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

public class CertExporter {

    public static void writePem(String filePath, String pemContent) throws IOException {
        Path path = Path.of(filePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, pemContent);
    }

    /**
     * Write a PKCS12 keystore to file, creating parent directories as needed.
     */
    public static void writePkcs12(String filePath, KeyStore ks, String password) throws Exception {
        Path path = Path.of(filePath);
        Files.createDirectories(path.getParent());
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            ks.store(fos, password.toCharArray());
        }
    }
}
