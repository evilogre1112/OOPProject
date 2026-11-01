package com.oop.Controller.Auth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.oop.Controller.Common.AuthHeader;
import com.oop.Controller.Common.FormField;
import com.oop.Controller.Common.LinkLabel;
import com.oop.Controller.Common.NakedButton;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;
import com.oop.Service.AccountService;

public class LoginPanel extends JPanel {
    private final AccountService accountService;
    private final Consumer<Account> onSuccess;

    private final JTextField txtEmail = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final FormField emailField = new FormField("Email", txtEmail);
    private final FormField passwordField = new FormField("Mật khẩu", txtPassword);

    public LoginPanel(AccountService accountService, Consumer<Account> onSuccess, Runnable onGoRegister) {
        this.accountService = accountService;
        this.onSuccess = onSuccess;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(56, 48, 56, 48));

        NakedButton btnLogin = new NakedButton("Đăng nhập");
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin()); // Enter ở ô mật khẩu cũng đăng nhập

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ask = new JLabel("Chưa có tài khoản?  ");
        ask.setFont(Theme.BODY);
        ask.setForeground(Theme.MUTED);
        footer.add(ask);
        footer.add(new LinkLabel("Tạo tài khoản", onGoRegister));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        add(new AuthHeader("Đăng nhập", "Chào mừng bạn quay lại."));
        add(Box.createRigidArea(new Dimension(0, 44)));
        add(emailField);
        add(Box.createRigidArea(new Dimension(0, 12)));
        add(passwordField);
        add(Box.createRigidArea(new Dimension(0, 24)));
        add(btnLogin);
        add(Box.createRigidArea(new Dimension(0, 24)));
        add(footer);
    }

    private void doLogin() {
        emailField.clearError();
        passwordField.clearError();

        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty()) {
            emailField.setError("Vui lòng nhập email");
            return;
        }
        if (password.isEmpty()) {
            passwordField.setError("Vui lòng nhập mật khẩu");
            return;
        }

        try {
            Account account = accountService.login(email, password);
            onSuccess.accept(account);
        } catch (SQLException ex) {
            passwordField.setError(ex.getMessage());
        }
    }

    /** Xoá dữ liệu đã nhập (gọi khi chuyển qua lại giữa các form). */
    public void clear() {
        txtEmail.setText("");
        txtPassword.setText("");
        emailField.clearError();
        passwordField.clearError();
    }
}
