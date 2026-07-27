# SSLCertTools (nginx-cert-tool)

A Java Swing GUI tool for generating self-signed Root CA and SSL/TLS server certificates — hassle-free, no OpenSSL command-line gymnastics required.

## Features

- **Root CA Certificate** — Generate a self-signed Certificate Authority with configurable subject, validity period, and RSA key size (2048 / 4096 bit).
- **Server Certificates** — Issue server certificates signed by your own CA, supporting SAN (Subject Alternative Name) with both DNS names and IP addresses.
- **Dual Output Formats:**
  - **Nginx** — Standard PEM files (`.crt` + `.key`), ready to drop into your `nginx.conf`.
  - **Tomcat** — PKCS12 keystore (`.p12`) bundling the server cert, private key, and CA chain — directly usable in `server.xml`.
- **One-click configuration snippets** — After generation, the tool prints ready-to-paste `nginx.conf` and `server.xml` config blocks.
- **Cross-platform** — Pure Java (JDK 17+), runs on Windows, macOS, and Linux.

## Screenshots

The main window has two tabs:

| Tab | Function |
|-----|----------|
| 根CA证书 (Root CA) | Generate the self-signed Root CA certificate |
| 服务器证书 (Server Cert) | Generate server certificates signed by the CA |

## Prerequisites

- **JDK 17** or later
- **Maven 3.6+** (for building from source)

## Quick Start

### Download & Run (recommended)

Download the latest `nginx-cert-tool-1.0.0.jar` from [Releases](../../releases), then:

```bash
java -jar nginx-cert-tool-1.0.0.jar
```

### Build from Source

```bash
git clone https://github.com/mypandastar/project.git
cd project/nginx-cert-tool
mvn clean package
java -jar target/nginx-cert-tool-1.0.0.jar
```

## Usage Guide

### 1. Generate a Root CA

1. Open the **根CA证书** tab.
2. Fill in the Common Name (e.g., `MyOrg Internal CA`).
3. Optionally set Organization, Org Unit, validity years, and key size.
4. Choose an output directory and click **生成证书**.

This produces:
```
<output-dir>/
├── ca.crt   ← CA certificate (PEM)
└── ca.key   ← CA private key (PEM)
```

### 2. Generate a Server Certificate

1. Open the **服务器证书** tab.
2. Enter the server Common Name (e.g., `api.example.com`).
3. Select the CA certificate and key files generated in step 1.
4. Check **Nginx** and/or **Tomcat** depending on your needs.
5. Add SAN entries (DNS names or IP addresses).
6. Click **生成证书**.

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17 |
| UI Toolkit | Swing |
| Crypto Library | [BouncyCastle](https://www.bouncycastle.org/) 1.78.1 |
| Build Tool | Maven (with shade plugin for fat JAR) |

## Project Structure

```
nginx-cert-tool/
├── pom.xml
├── src/main/java/com/certtool/
│   ├── App.java                 # Entry point
│   ├── TestRunner.java          # CLI test runner
│   ├── model/
│   │   ├── CaConfig.java        # CA generation parameters
│   │   ├── CertResult.java      # Single certificate generation result
│   │   ├── ServerCertConfig.java      # Server cert generation parameters
│   │   └── ServerCertGenerationResult.java  # Aggregated result (Nginx + Tomcat)
│   ├── service/
│   │   ├── CertGenerator.java   # Core certificate generation logic
│   │   └── CertExporter.java    # PEM / PKCS12 file output
│   └── ui/
│       ├── MainFrame.java       # Main window
│       ├── RootCaPanel.java     # Root CA tab
│       ├── ServerCertPanel.java # Server certificate tab
│       └── StyleConstants.java  # UI styling constants
├── start.bat                    # Windows launcher
└── start.sh                     # Linux/macOS launcher
```

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Author

**Panda** — [mypandastar](https://github.com/mypandastar)
