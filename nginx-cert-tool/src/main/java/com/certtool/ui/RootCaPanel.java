package com.certtool.ui;

import com.certtool.model.CaConfig;
import com.certtool.model.CertResult;
import com.certtool.service.CertGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class RootCaPanel extends JPanel {

    private final JTextField cnField = new JTextField(StyleConstants.FIELD_WIDTH);
    private final JTextField orgField = new JTextField(StyleConstants.FIELD_WIDTH);
    private final JTextField ouField = new JTextField(StyleConstants.FIELD_WIDTH);
    private final JComboBox<String> validityCombo = new JComboBox<>(
            new String[]{"1", "2", "5", "10", "20"});
    private final JComboBox<String> keySizeCombo = new JComboBox<>(
            new String[]{"2048", "4096"});
    private final JTextField outputDirField = new JTextField(StyleConstants.FILE_FIELD_WIDTH);
    private final JButton browseBtn = new JButton("浏览");
    private final JButton generateBtn = new JButton("生成证书");
    private final JLabel statusLabel = new JLabel(" ");

    public RootCaPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(
                StyleConstants.DIALOG_PADDING, StyleConstants.DIALOG_PADDING,
                StyleConstants.DIALOG_PADDING, StyleConstants.DIALOG_PADDING));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildResultPanel(), BorderLayout.CENTER);

        validityCombo.setSelectedItem("10");
        keySizeCombo.setSelectedItem("2048");

        browseBtn.addActionListener(e -> browseOutputDir());
        generateBtn.addActionListener(e -> doGenerate());
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;
        // Row 0: Common Name
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Common Name (通用名称):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(cnField, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        // Row 1: Organization
        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Organization (组织):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(orgField, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        // Row 2: Org Unit
        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Org Unit (组织单位):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(ouField, gc);
        gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE;

        // Row 3: Validity
        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Validity/有效期 (年):"), gc);
        gc.weightx = 0; gc.gridx = 1; panel.add(validityCombo, gc);

        // Row 4: Key Size
        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Key Size (密钥长度):"), gc);
        gc.weightx = 0; gc.gridx = 1; panel.add(keySizeCombo, gc);

        // Row 5: Output Dir
        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(label("Output Dir (输出目录):"), gc);
        gc.weightx = 1.0; gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(outputDirField, gc);
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0; gc.gridx = 2; panel.add(browseBtn, gc);

        // Row 6: Generate button
        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 3;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(generateBtn);
        panel.add(btnPanel, gc);

        return panel;
    }

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

    private void browseOutputDir() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputDirField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void doGenerate() {
        if (cnField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Common Name is required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String outputDir = outputDirField.getText().trim();
        if (outputDir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Output directory is required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CaConfig config = new CaConfig();
        config.setCommonName(cnField.getText().trim());
        config.setOrganization(orgField.getText().trim());
        config.setOrganizationalUnit(ouField.getText().trim());
        config.setValidityYears(Integer.parseInt((String) validityCombo.getSelectedItem()));
        config.setKeySize(Integer.parseInt((String) keySizeCombo.getSelectedItem()));

        generateBtn.setEnabled(false);
        statusLabel.setText("Generating...");

        new SwingWorker<CertResult, Void>() {
            @Override
            protected CertResult doInBackground() throws Exception {
                return new CertGenerator().generateRootCa(config, outputDir);
            }

            @Override
            protected void done() {
                generateBtn.setEnabled(true);
                try {
                    CertResult result = get();
                    String nginxConfig = """
                            server {
                                listen 443 ssl;
                                ssl_certificate %s;
                                ssl_certificate_key %s;
                            }""".formatted(result.getCertPath(), result.getKeyPath());
                    statusLabel.setText("<html>✅ Root CA generated<br>ca.crt: %s<br>ca.key: %s<br><br>Nginx config:<br><pre>%s</pre></html>"
                            .formatted(result.getCertPath(), result.getKeyPath(), nginxConfig));
                } catch (Exception e) {
                    statusLabel.setText(" ");
                    JOptionPane.showMessageDialog(RootCaPanel.this,
                            "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
