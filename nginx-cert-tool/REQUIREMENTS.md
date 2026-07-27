# Nginx SSL 证书生成工具 — 需求变更计划

## 版本：v1.1.0（从 v1.0.0 升级）

---

## 一、需求概述

当前工具仅在"服务器证书"Tab 页生成 **Nginx 专用**的 PEM 格式证书（`.crt` + `.key`）。
现需扩展为支持 **两种用途** 的服务器证书生成：

| 用途 | 输出格式 | 说明 |
|------|---------|------|
| **Nginx** | PEM（`.crt` + `.key`） | 与现有逻辑一致，证书 + 私钥分离文件 |
| **Tomcat** | PKCS12 密钥库（`.p12`） | 证书 + 私钥 + CA 链打包为一个 keystore，需设置密码 |

两种证书的 X.509 内容逻辑一致（均为 TLS Server Auth），区别在于 **输出封装格式**。

---

## 二、UI 变更

### 2.1 服务器证书 Tab 页（`ServerCertPanel`）

**新增控件：**

```
┌─────────────────────────────────────────────────┐
│ ☑ Nginx 证书                                    │
│   Output Dir (输出目录): [________] [浏览]       │
│                                                 │
│ ☐ Tomcat 证书                                   │
│   Output Dir (输出目录): [________] [浏览]       │
│   Keystore Password (密钥库密码): [________]     │
└─────────────────────────────────────────────────┘
```

**交互逻辑：**
- `Nginx 证书` 勾选框：默认勾选（保持向后兼容）
- `Tomcat 证书` 勾选框：默认不勾选
- 勾选后才启用对应的输出目录输入框和浏览按钮
- Tomcat 勾选后额外显示密码输入框
- **至少勾选一种**才能点击"生成证书"按钮
- 生成成功后状态栏分别显示 Nginx/Tomcat 的输出路径和配置示例

### 2.2 主窗口标题（`MainFrame`）

```
修改前：Nginx SSL 证书生成工具
修改后：SSL 证书生成工具 (Nginx / Tomcat)
```

---

## 三、Model 层变更

### 3.1 `ServerCertConfig` 新增字段

```java
// --- 新增字段 ---

// 是否生成 Nginx 格式证书
private boolean nginxEnabled = true;

// Nginx 证书输出目录
private String nginxOutputDir;

// 是否生成 Tomcat 格式证书
private boolean tomcatEnabled = false;

// Tomcat 证书输出目录
private String tomcatOutputDir;

// Tomcat keystore 密码（至少6位）
private String tomcatKeystorePassword;
```

### 3.2 `CertResult` 扩展

原有字段仅描述单组输出。改为支持多种输出结果的聚合：

```java
// 新增：Nginx 子结果（null 表示未生成）
private CertResult nginxResult;

// 新增：Tomcat 子结果（null 表示未生成）
private CertResult tomcatResult;

// 兼容：保留原 getCertPath()/getKeyPath()，默认指向 nginxResult
```

---

## 四、Service 层变更

### 4.1 `CertGenerator`

**新增方法：**

```java
/**
 * Generate PKCS12 keystore for Tomcat.
 * Bundles server cert + private key + CA cert chain into a .p12 file.
 */
public CertResult generateTomcatKeystore(ServerCertConfig config, String outputDir, String password)
        throws Exception
```

**逻辑说明：**
1. 复用现有 `generateServerCert` 生成 X.509 证书和密钥对
2. 将服务端证书 + 私钥 + CA 证书链打包为 PKCS12 格式
3. 输出文件名为 `{commonName}.p12`
4. 返回 `CertResult`，其中 `certPath` 指向 `.p12` 文件

**PKCS12 生成细节（使用 BouncyCastle）：**
```java
KeyStore ks = KeyStore.getInstance("PKCS12");
ks.load(null, null);
ks.setKeyEntry("server", serverPrivateKey, password.toCharArray(),
        new X509Certificate[]{serverCert, caCert});
try (FileOutputStream fos = new FileOutputStream(outputPath)) {
    ks.store(fos, password.toCharArray());
}
```

### 4.2 `CertExporter`

新增静态方法：

```java
/** Write PKCS12 keystore to file */
public static void writePkcs12(String filePath, KeyStore ks, String password) throws Exception
```

---

## 五、UI 逻辑变更（`ServerCertPanel.doGenerate`）

当前流程 → 新流程：

```
旧：收集参数 → 校验 → 单一生成 → 显示 Nginx config
新：收集参数 → 校验（至少勾选一种 + 各自输出目录非空 + Tomcat密码非空）
    → 并行生成选中的类型
    → 状态栏汇总显示：
        ☑ Nginx:  cert → /path/xxx.crt, key → /path/xxx.key
           Nginx config snippet
        ☑ Tomcat: keystore → /path/xxx.p12
           Tomcat server.xml snippet
```

**Tomcat 配置示例（server.xml）：**
```xml
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
           maxThreads="150" scheme="https" secure="true"
           keystoreFile="/path/xxx.p12"
           keystorePass="your_password"
           keystoreType="PKCS12"
           clientAuth="false" sslProtocol="TLS" />
```

---

## 六、涉及文件清单

| 文件 | 变更类型 | 说明 |
|------|---------|------|
| `ServerCertPanel.java` | **重写** | 最大变更，新增勾选框、双输出目录、密码字段 |
| `ServerCertConfig.java` | **扩展** | 新增 5 个字段 |
| `CertResult.java` | **扩展** | 支持多输出结果聚合 |
| `CertGenerator.java` | **新增方法** | 新增 `generateTomcatKeystore` |
| `CertExporter.java` | **新增方法** | 新增 `writePkcs12` |
| `MainFrame.java` | **微调** | 修改窗口标题 |

---

## 七、验证检查点

- [ ] 仅勾选 Nginx → 生成 `.crt` + `.key`，与 v1.0 行为一致
- [ ] 仅勾选 Tomcat → 生成 `.p12` keystore
- [ ] 同时勾选 → 两组输出都生成到各自目录
- [ ] 两种都不勾选 → 按钮不启用 / 校验拦截
- [ ] Tomcat 密码为空 → 校验拦截
- [ ] Tomcat 密码少于6位 → 校验拦截
- [ ] 输出目录不存在 → 自动创建
- [ ] 生成成功后状态栏同时展示 Nginx 和 Tomcat 的配置示例

---

## 八、实现顺序

1. **Model 层**：扩展 `ServerCertConfig`、`CertResult`
2. **Service 层**：`CertGenerator.generateTomcatKeystore`、`CertExporter.writePkcs12`
3. **UI 层**：改造 `ServerCertPanel`
4. **主窗口**：调整 `MainFrame` 标题
5. **编译测试**：Maven 编译 + 启动验证

---

## 九、版本 v1.2.0 — 关于对话框（已完成）

### 9.1 需求描述

在界面适当位置添加"关于"按钮，点击后弹出对话框展示软件版权信息、作者、版本号等。

### 9.2 UI 变更（`MainFrame`）

- 主窗口布局重构：外层 `JPanel(BorderLayout)` → CENTER 放 `JTabbedPane`，SOUTH 放 `JPanel(FlowLayout.RIGHT)` 包含"关于"按钮
- 点击"关于"弹出 `JOptionPane` 对话框，以 HTML 格式展示：
  - 软件名称及版本
  - 功能简述
  - 技术栈（JDK 17 / Swing / BouncyCastle）
  - 版权声明

### 9.3 涉及文件

| 文件 | 变更 | 说明 |
|------|------|------|
| `MainFrame.java` | 修改 | 布局重构 + "关于"按钮 + 弹窗方法 |
| `REQUIREMENTS.md` | 追加 | 本文档本条目 |

### 9.4 验证检查点

- [ ] 窗口右下角可见"关于"按钮
- [ ] 点击弹出对话框，内容正确
- [ ] 对话框可正常关闭
