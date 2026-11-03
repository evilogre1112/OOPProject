package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.Controller.Common.LinkLabel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;

/** Lobby demo của cửa hàng. Logic thật sẽ thêm sau. */
public class LobbyFrame extends JFrame {
    public LobbyFrame(Account account, Runnable onLogout) {
        setTitle("NAKED");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(createHeader(account, onLogout), BorderLayout.NORTH);
        root.add(createCollection(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createHeader(Account account, Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout(32, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
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
        user.add(new LinkLabel("Đăng xuất", () -> {
            dispose();
            onLogout.run();
        }));

        header.add(brand, BorderLayout.WEST);
        header.add(nav, BorderLayout.CENTER);
        header.add(user, BorderLayout.EAST);
        return header;
    }

    /** 4 ô màu giả lập ảnh sản phẩm, cho giống bố cục trong ảnh mẫu. */
    private JPanel createCollection() {
        Color[] colors = {
                new Color(0xE8B4C4), // hồng
                new Color(0x6B1E3A), // đỏ đô
                new Color(0xD9DDB8), // xanh sage
                new Color(0xF2C9D0) // hồng nhạt
        };
        String[] names = { "Pink Dress", "Burgundy Slip", "Sage Slip", "Cherrie Dress" };

        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        for (int i = 0; i < colors.length; i++) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(colors[i]);
            card.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));

            JLabel name = new JLabel(names[i]);
            name.setFont(new Font("Georgia", Font.ITALIC, 18));
            name.setForeground(i == 1 ? Color.WHITE : Theme.INK);
            card.add(name, BorderLayout.SOUTH);
            grid.add(card);
        }
        return grid;
    }
}
