package com.oop.Controller.Common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/** Mục menu dọc: chữ hoa giãn chữ, mục đang chọn nền đen chữ trắng. */
public class NavItem extends JLabel {
    private boolean selected = false;

    public NavItem(String text, Runnable onClick) {
        super(text.toUpperCase());
        setFont(Theme.spaced(Theme.LABEL.deriveFont(12f), 0.12f));
        setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        setOpaque(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        setSelected(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!selected) {
                    setBackground(Theme.LINE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setSelected(selected);
            }
        });
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setBackground(selected ? Theme.INK : Theme.BG);
        setForeground(selected ? Color.WHITE : Theme.INK);
    }
}
