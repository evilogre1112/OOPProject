package com.oop;

import javax.swing.*;

import com.oop.Controller.Auth.LoginComponent;

import java.awt.*;

public class LoginFrameDemo extends JFrame {

    public LoginFrameDemo() {
        setTitle("Login UI");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Giữa màn hình
        getContentPane().setBackground(Color.DARK_GRAY);

        JPanel mainPanel = new LoginComponent();
        add(mainPanel);
    }

    public static void main(String[] args) {
        // Chạy UI với giao diện mặc định của hệ thống cho đồng bộ
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrameDemo().setVisible(true);
        });
    }
}