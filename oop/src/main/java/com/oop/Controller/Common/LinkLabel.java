package com.oop.Controller.Common;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JLabel;

/** Chữ gạch chân kiểu link trên menu website. Click sẽ chạy onClick. */
public class LinkLabel extends JLabel {
    public LinkLabel(String text, Runnable onClick) {
        super(text);
        setFont(Theme.BODY.deriveFont(Map.of(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON)));
        setForeground(Theme.INK);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setForeground(Theme.ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(Theme.INK);
            }
        });
    }
}
