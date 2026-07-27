package com.certtool.ui;

import com.certtool.model.CertResult;
import com.certtool.model.ServerCertConfig;
import com.certtool.model.ServerCertGenerationResult;
import com.certtool.service.CertGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerCertPanel extends JPanel {

    // --- common fields ---
    private final JTextField cnField = new JTextField(StyleConstants.FIELD_WIDTH);
    private final JTextField sanDnsField = new JTextField(StyleConstants.FIELD_WIDTH);
    private final JTextField sanIpField = new JTextField(StyleConstants.FIELD_WIDTH);
    private final JTextField validityField = new JTextField("365", StyleConstants.FIELD_WIDTH);
    private final JComboBox<String> keySizeCombo = new JComboBox<>(
            new String[]{"2048", "4096"});

    // --- CA fields ---
    private final JTextField caCertField = new JTextField(StyleConstants.FILE_FIELD_WIDTH);
    private final JButton caCertBrowseBtn = new JButton("浏览");
    private final JTextField caKeyField = new JTextField(StyleConstants.FILE_FIELD_WIDTH);
    private final JButton caKeyBrowseBtn = new JButton("浏览");

    // --- Nginx fields ---
    private final JCheckBox nginxCheckBox = new JCheckBox("Nginx 证书 (PEM格式: .crt + .key)");
    private final JTextField nginxOutputDirField = new JTextField(StyleConstants.FILE_FIELD_WIDTH);
    private final JButton nginxOutputBrowseBtn = new JButton("浏览");

    // --- Tomcat fields ---
    private final JCheckBox tomcatCheckBox = new JCheckBox("Tomcat 证书 (PKCS12格式: .p12)");
    private final JTextField tomcatOutputDirField = new JTextField(StyleConstants.FILE_FIELD_WIDTH);
    private final JButton tomcatOutputBrowseBtn = new JButton("浏览");
    private final JPasswordField tomcatPasswordField = new JPasswordField(StyleConstants.FIELD_WIDTH);

    // --- action ---
    private final JButton generateBtn = new JButton("生成证书");
    private final JLabel statusLabel = new JLabel(" ");

    public ServerCertPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(
                StyleConstants.DIALOG_PADDING, StyleConstants.DIALOG_PADDING,
                StyleConstants.DIALOG_PADDING, StyleConstants.DIALOG_PADDING));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildResultPanel(), BorderLayout.CENTER);

        keySizeCombo.setSelectedItem("2048");
        nginxCheckBox.setSelected(true);
        updateFieldStates();

        // --- bind events ---
        caCertBrowseBtn.addActionListener(e -> browseFile(caCertField, "CA Certificate"));
        caKeyBrowseBtn.addActionListener(e -> browseFile(caKeyField, "CA Private Key"));
        nginxOutputBrowseBtn.addActionListener(e -> browseDir(nginxOutputDirField));
        tomcatOutputBrowseBtn.addActionListener(e -> browseDir(tomcatOutputDirField));

        nginxCheckBox.addActionListener(e -> updateFieldStates());
        tomcatCheckBox.addActionListener(e -> updateFieldStates());

        generateBtn.addActionListener(e -> doGenerate());
    }

    /** Enable/disable output fields based on checkbox selection. */
    private void updateFieldStates() {
        boolean nginxSel = nginxCheckBox.isSelected();
        nginxOutputDirField.setEnabled(nginxSel);
        nginxOutputBrowseBtn.setEnabled(nginxSel);

        boolean tomcatSel = tomcatCheckBox.isSelected();
        tomcatOutputDirField.setEnabled(tomcatSel);
        tomcatOutputBrowseBtn.setEnabled(tomcatSel);
        tomcatPasswordField.setEnabled(tomcatSel);

        // At least one must be selected
        generateBtn.setEnabled(nginxSel || tomcatSel);
    }

    // region form layout
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // --- Common ---
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 0;
        panel.add(separatorLabel("通用设置"), gc);
        gc.gridwidth = 1;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Domain/CN (域名):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cnField, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("SAN DNS (可选DNS):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(sanDnsField, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("SAN IP (可选IP):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(sanIpField, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Validity/有效期 (天):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(validityField, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Key Size (密钥长度):"), gc);
        gc.weightx = 0; gc.gridx = 1; panel.add(keySizeCombo, gc);

        // --- CA ---
        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 0;
        panel.add(separatorLabel("CA 证书"), gc);
        gc.gridwidth = 1;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("CA Certificate (CA证书):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(caCertField, gc);
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0; gc.gridx = 2; panel.add(caCertBrowseBtn, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("CA Private Key (CA私钥):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(caKeyField, gc);
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0; gc.gridx = 2; panel.add(caKeyBrowseBtn, gc);

        // --- Nginx ---
        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 0;
        panel.add(nginxCheckBox, gc);
        gc.gridwidth = 1;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("  Output Dir (输出目录):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nginxOutputDirField, gc);
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0; gc.gridx = 2; panel.add(nginxOutputBrowseBtn, gc);

        // --- Tomcat ---
        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 0;
        panel.add(tomcatCheckBox, gc);
        gc.gridwidth = 1;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("  Output Dir (输出目录):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tomcatOutputDirField, gc);
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0; gc.gridx = 2; panel.add(tomcatOutputBrowseBtn, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("  Keystore Password (密钥库密码):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tomcatPasswordField, gc);
        gc.fill = GridBagConstraints.NONE;

        // --- Generate button ---
        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 3; gc.weightx = 0;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(generateBtn);
        panel.add(btnPanel, gc);

        return panel;
    }
    // endregion

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(statusLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(StyleConstants.LABEL_FONT);
        return lbl;
    }

    private JLabel separatorLabel(String text) {
        JLabel lbl = new JLabel("── " + text + " ──");
        lbl.setFont(StyleConstants.LABEL_FONT.deriveFont(java.awt.Font.BOLD));
        return lbl;
    }

    private void browseFile(JTextField field, String title) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void browseDir(JTextField field) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void doGenerate() {
        // --- common validation ---
        if (cnField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Domain (CN) is required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (caCertField.getText().isBlank() || caKeyField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "CA certificate and key are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean nginxSel = nginxCheckBox.isSelected();
        boolean tomcatSel = tomcatCheckBox.isSelected();

        if (!nginxSel && !tomcatSel) {
            JOptionPane.showMessageDialog(this, "At least one certificate type must be selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nginxOutputDir = nginxOutputDirField.getText().trim();
        if (nginxSel) {
            if (nginxOutputDir.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nginx output directory is required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String tomcatOutputDir = tomcatOutputDirField.getText().trim();
        String tomcatPassword = new String(tomcatPasswordField.getPassword());
        if (tomcatSel) {
            if (tomcatOutputDir.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tomcat output directory is required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (tomcatPassword.length() < 6) {
                JOptionPane.showMessageDialog(this, "Tomcat keystore password must be at least 6 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int validityDays;
        try {
            validityDays = Integer.parseInt(validityField.getText().trim());
            if (validityDays <= 0) throw new NumberFormatException("Must be positive");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Validity/有效期 must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- build config ---
        ServerCertConfig config = new ServerCertConfig();
        config.setCommonName(cnField.getText().trim());
        config.setSanDns(parseList(sanDnsField.getText()));
        config.setSanIp(parseList(sanIpField.getText()));
        config.setValidityDays(validityDays);
        config.setKeySize(Integer.parseInt((String) keySizeCombo.getSelectedItem()));
        config.setCaCertPath(caCertField.getText().trim());
        config.setCaKeyPath(caKeyField.getText().trim());

        config.setNginxEnabled(nginxSel);
        config.setNginxOutputDir(nginxOutputDir);
        config.setTomcatEnabled(tomcatSel);
        config.setTomcatOutputDir(tomcatOutputDir);
        config.setTomcatKeystorePassword(tomcatPassword);

        // --- execute ---
        generateBtn.setEnabled(false);
        statusLabel.setText("Generating...");

        new SwingWorker<ServerCertGenerationResult, Void>() {
            @Override
            protected ServerCertGenerationResult doInBackground() throws Exception {
                return new CertGenerator().generateServerCerts(config);
            }

            @Override
            protected void done() {
                generateBtn.setEnabled(true);
                try {
                    ServerCertGenerationResult result = get();
                    statusLabel.setText(buildResultHtml(config, result));
                } catch (Exception e) {
                    statusLabel.setText(" ");
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(ServerCertPanel.this,
                            "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String buildResultHtml(ServerCertConfig config, ServerCertGenerationResult result) {
        StringBuilder sb = new StringBuilder("<html>");

        CertResult nginxR = result.getNginxResult();
        if (nginxR != null) {
            String nginxConfig = """
                    server {
                        listen 443 ssl;
                        server_name %s;
                        ssl_certificate %s;
                        ssl_certificate_key %s;
                    }""".formatted(config.getCommonName(), nginxR.getCertPath(), nginxR.getKeyPath());
            sb.append("✅ <b>Nginx 证书已生成</b><br>")
                    .append("&nbsp;&nbsp;证书: ").append(escapeHtml(nginxR.getCertPath())).append("<br>")
                    .append("&nbsp;&nbsp;私钥: ").append(escapeHtml(nginxR.getKeyPath())).append("<br>")
                    .append("<br>Nginx 配置:<br><pre>")
                    .append(escapeHtml(nginxConfig)).append("</pre>");
        }

        CertResult tomcatR = result.getTomcatResult();
        if (tomcatR != null) {
            if (nginxR != null) {
                sb.append("<br><hr><br>");
            }
            String tomcatConfig = """
                    <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
                               maxThreads="150" scheme="https" secure="true"
                               keystoreFile="%s"
                               keystorePass="%s"
                               keystoreType="PKCS12"
                               clientAuth="false" sslProtocol="TLS" />"""
                    .formatted(tomcatR.getCertPath(), config.getTomcatKeystorePassword());
            sb.append("✅ <b>Tomcat 证书已生成</b><br>")
                    .append("&nbsp;&nbsp;Keystore: ").append(escapeHtml(tomcatR.getCertPath())).append("<br>")
                    .append("<br>Tomcat server.xml 配置:<br><pre>")
                    .append(escapeHtml(tomcatConfig)).append("</pre>");
        }

        sb.append("</html>");
        return sb.toString();
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private List<String> parseList(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
