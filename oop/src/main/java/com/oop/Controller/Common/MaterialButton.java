package com.oop.Controller.Common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MaterialButton extends JButton {
    private Color normalColor = new Color(41, 182, 246);
    private Color hoverColor = new Color(29, 155, 214);

    private boolean isHovering = false;

    public MaterialButton(String text) {
        this(text, "SansSerif", Color.WHITE, 14);
    }

    public MaterialButton(String text, int size) {
        this(text, "SansSerif", Color.WHITE, size);
    }

    public MaterialButton(String text, String fontName, Color textColor, int size) {
        super(text);
        setFont(new Font(fontName, Font.BOLD, size));
        setForeground(textColor);
        setBackground(normalColor);

        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                isHovering = true;
                setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                isHovering = false;
                setBackground(normalColor);
            }
        });
    }

    public void setNormalColor(Color normalColor) {
        this.normalColor = normalColor;
        if (!isHovering) {
            setBackground(this.normalColor);
        }
    }

    public void setHoverColor(Color hoverColor) {
        this.hoverColor = hoverColor;
        if (isHovering) {
            setBackground(this.hoverColor);
        }
    }
}