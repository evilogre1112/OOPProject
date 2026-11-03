package com.oop.Controller.Common;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Hàng nút thao tác phía trên bảng (Thêm / Sửa / Xóa ...), dùng lại NakedButton. */
public class ActionBar extends JPanel {
    public ActionBar() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
    }

    /** Thêm một nút; độ rộng co giãn theo chữ. */
    public NakedButton button(String text, Runnable action) {
        NakedButton button = new NakedButton(text);
        button.setPreferredSize(new Dimension(Math.max(110, 40 + text.length() * 11), 38));
        button.addActionListener(e -> action.run());
        addField(button);
        return button;
    }

    /** Thêm nhãn nhỏ + ô nhập (gạch chân) vào hàng nút và trả về ô nhập. */
    public JTextField textField(String label, String value, int width) {
        JTextField field = new JTextField(value);
        Theme.styleField(field);
        field.setPreferredSize(new Dimension(width, 38));
        addField(Theme.fieldLabel(label));
        addField(field);
        return field;
    }

    /** Thêm một component bất kỳ (vd: ô tìm kiếm) kèm khoảng cách bên phải. */
    public void addField(JComponent component) {
        add(component);
        add(Box.createRigidArea(new Dimension(8, 0)));
    }
}
