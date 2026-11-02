package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.Controller.Common.LinkLabel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;

/** Thanh trên cùng: logo, menu, lời chào + đăng xuất. */
public class LobbyHeader extends JPanel {
    public LobbyHeader(Account account, Runnable onLogout) {
        super(new BorderLayout(32, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(20, 32, 20, 32)));

        JLabel brand = new JLabel("NAKED");
        brand.setFont(Theme.spaced(new Font("SansSerif", Font.PLAIN, 26), 0.4f));
        brand.setForeground(Theme.INK);

        // Menu giống thanh điều hướng trên website (chưa gắn chức năng)
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 6));
        nav.setOpaque(false);
        nav.add(new LinkLabel("Shop Now", () -> {
        }));
        nav.add(new LinkLabel("New Arrivals", () -> {
        }));
        nav.add(new LinkLabel("Sale", () -> {
        }));

        JPanel user = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 6));
        user.setOpaque(false);
        JLabel hello = new JLabel("Xin chào, " + account.getNickName());
        hello.setFont(Theme.BODY);
        hello.setForeground(Theme.MUTED);
        user.add(hello);
        user.add(new LinkLabel("Đăng xuất", onLogout));

        add(brand, BorderLayout.WEST);
        add(nav, BorderLayout.CENTER);
        add(user, BorderLayout.EAST);
    }
}
