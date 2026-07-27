package com.certtool.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;

public class MainFrame extends JFrame {

    private static final String ABOUT_HTML = """
            <html>
            <div style='text-align:left; padding:8px;'>
            <b style='font-size:14px;'>SSLCertTools</b><br><br>
            版本：v1.1.0<br><br>
            遵循协议：Apache 2.0 开源协议<br><br>
            作者：Panda
            </div>
            </html>
            """;

    public MainFrame() throws HeadlessException {
        setTitle("SSL 证书生成工具 (Nginx / Tomcat)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(680, 560));
        setSize(700, 620);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("根CA证书", new RootCaPanel());
        tabs.addTab("服务器证书", new ServerCertPanel());

        // About link at bottom-right corner
        JLabel aboutLink = new JLabel("关于");
        aboutLink.setForeground(new Color(100, 100, 100));
        aboutLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        aboutLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showAboutDialog();
            }
        });
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bottomPanel.add(aboutLink);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabs, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);

        setLocationRelativeTo(null);
    }

    private void showAboutDialog() {
        JLabel label = new JLabel(ABOUT_HTML);
        JOptionPane.showMessageDialog(this, label, "关于",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
