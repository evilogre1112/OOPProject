package com.oop.Controller.Common;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class MaterialTextField extends JTextField {
    private Color inactiveColor = new Color(200, 200, 200);
    private Color activeColor = new Color(41, 182, 246);

    public MaterialTextField() {
        this("SansSerif", Color.DARK_GRAY, 14);
    }

    public MaterialTextField(int size) {
        this("SansSerif", Color.DARK_GRAY, size);
    }

    public MaterialTextField(String fontName, Color color, int size) {
        super();
        setOpaque(false);
        setFont(new Font(fontName, Font.PLAIN, size));
        setForeground(color);
        updateBorder(inactiveColor);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBorder(activeColor);
            }

            @Override
            public void focusLost(FocusEvent e) {
                updateBorder(inactiveColor);
            }
        });
    }

    private void updateBorder(Color color) {
        MatteBorder matteBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, color);
        EmptyBorder emptyBorder = new EmptyBorder(5, 0, 5, 0);
        setBorder(new CompoundBorder(matteBorder, emptyBorder));
    }

    public void setActiveColor(Color activeColor) {
        this.activeColor = activeColor;
        if (hasFocus()) {
            updateBorder(this.activeColor);
        }
    }

    public void setInactiveColor(Color inactiveColor) {
        this.inactiveColor = inactiveColor;
        if (!hasFocus()) {
            updateBorder(this.inactiveColor);
        }
    }
}