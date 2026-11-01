package com.oop.Controller.Common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/** Nút vuông, nền đen, hover đổi sang đỏ đô. Tự vẽ nền nên không bị Look&Feel (Aqua trên macOS) ghi đè. */
public class NakedButton extends JButton {
    private boolean hovering = false;

    public NakedButton(String text) {
        super(text.toUpperCase());
        setFont(Theme.spaced(Theme.LABEL.deriveFont(12f), 0.15f));
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(200, 46));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(!isEnabled() ? Theme.LINE : hovering ? Theme.ACCENT : Theme.INK);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g); // vẽ chữ
    }
}
