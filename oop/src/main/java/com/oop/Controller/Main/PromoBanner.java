package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.oop.Controller.Common.Theme;

/** Dải chữ thông báo/ưu đãi nằm giữa các khối sản phẩm. */
public class PromoBanner extends JPanel {
    public PromoBanner(String text) {
        super(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));
        setPreferredSize(new Dimension(0, 96));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(Theme.spaced(Theme.SERIF_TITLE.deriveFont(26f), 0.04f));
        label.setForeground(Theme.INK);
        add(label, BorderLayout.CENTER);
    }
}
