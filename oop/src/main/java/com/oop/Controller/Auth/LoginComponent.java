package com.oop.Controller.Auth;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.Controller.Common.MaterialButton;
import com.oop.Controller.Common.MaterialPasswordField;
import com.oop.Controller.Common.MaterialTextField;

public class LoginComponent extends JPanel {
    public LoginComponent() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(50, 40, 50, 40));

        // 1. Tiêu đề "Sign In"
        JLabel titleLabel = new JLabel("Sign In");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 2. Sử dụng component custom
        MaterialTextField txtUsername = new MaterialTextField();
        txtUsername.setText("raven");

        MaterialPasswordField txtPassword = new MaterialPasswordField();
        txtPassword.setText("12345678");

        MaterialButton btnLogin = new MaterialButton("Login");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Nút rộng tràn viền, cao 40px
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.add(titleLabel);
        this.add(Box.createRigidArea(new Dimension(0, 50))); // Khoảng cách
        this.add(createInputGroup("Username", txtUsername));
        this.add(Box.createRigidArea(new Dimension(0, 25)));
        this.add(createInputGroup("Password", txtPassword));
        this.add(Box.createRigidArea(new Dimension(0, 40)));
        this.add(btnLogin);
    }

    private JPanel createInputGroup(String labelText, JComponent inputField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(new Color(170, 170, 170)); // Màu xám nhạt

        panel.add(lbl);
        panel.add(inputField);

        // Cấu hình để ô textfield có thể kéo dài hết chiều ngang
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return panel;
    }
}
