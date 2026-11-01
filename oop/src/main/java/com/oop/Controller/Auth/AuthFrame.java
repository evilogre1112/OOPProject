package com.oop.Controller.Auth;

import java.awt.CardLayout;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.oop.Model.Account;
import com.oop.Service.AccountService;

/**
 * Frame mẫu để chạy thử: chuyển qua lại giữa Login và Register bằng CardLayout.
 */
public class AuthFrame extends JFrame {
    public AuthFrame(AccountService accountService, Consumer<Account> onLoginSuccess) {
        CardLayout cards = new CardLayout();
        JPanel root = new JPanel(cards);

        LoginPanel login = new LoginPanel(
                accountService,
                account -> {
                    dispose(); // đóng frame auth rồi mới mở lobby
                    onLoginSuccess.accept(account);
                },
                () -> cards.show(root, "register"));

        RegisterPanel register = new RegisterPanel(
                () -> cards.show(root, "login"),
                () -> cards.show(root, "login"));

        root.add(login, "login");
        root.add(register, "register");

        setTitle("NAKED");
        setContentPane(root);
        setSize(480, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
