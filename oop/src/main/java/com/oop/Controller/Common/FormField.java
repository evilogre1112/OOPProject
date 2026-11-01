package com.oop.Controller.Common;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/** Nhãn + ô nhập (gạch chân) + dòng báo lỗi. Dùng được cho JTextField và JPasswordField. */
public class FormField extends JPanel {
    private final JLabel errorLabel = new JLabel(" "); // " " để giữ chỗ, form không bị nhảy

    public FormField(String label, JTextComponent input) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);

        Theme.styleField(input);
        input.setAlignmentX(LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, input.getPreferredSize().height));

        errorLabel.setFont(Theme.SMALL);
        errorLabel.setForeground(Theme.ERROR);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);

        add(Theme.fieldLabel(label));
        add(input);
        add(errorLabel);
    }

    public void setError(String message) {
        errorLabel.setText(message == null || message.isEmpty() ? " " : message);
    }

    public void clearError() {
        setError(null);
    }
}
