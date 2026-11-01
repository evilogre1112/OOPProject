package com.oop.Controller.Common;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Phần đầu form: logo NAKED, tiêu đề chữ serif nghiêng, dòng mô tả. */
public class AuthHeader extends JPanel {
    public AuthHeader(String title, String subtitle) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);

        JLabel brand = new JLabel("NAKED");
        brand.setFont(Theme.spaced(new Font("SansSerif", Font.PLAIN, 22), 0.4f));
        brand.setForeground(Theme.INK);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.SERIF_TITLE);
        titleLbl.setForeground(Theme.INK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(Theme.BODY);
        subLbl.setForeground(Theme.MUTED);

        add(brand);
        add(Box.createRigidArea(new Dimension(0, 36)));
        add(titleLbl);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(subLbl);
    }
}
