package com.oop;

import javax.swing.SwingUtilities;

import com.oop.Controller.Auth.AuthFrame;
import com.oop.Controller.Main.LobbyFrame;
import com.oop.Model.Account;
import com.oop.Service.AccountService;

public class Main {
    private static final AccountService accountService = new AccountService();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showAuth);
    }

    private static void showAuth() {
        new AuthFrame(accountService, Main::showLobby).setVisible(true);
    }

    private static void showLobby(Account account) {
        new LobbyFrame(accountService, () -> {
            accountService.logout();
            showAuth();
        }).setVisible(true);
    }
}