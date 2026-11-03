package com.oop.Controller.Common;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Hộp thoại nhập liệu dùng chung. Ví dụ:
 * <pre>
 * String[] v = new FormDialog(this, "Thêm danh mục").text("Tên", "").showDialog();
 * if (v == null) return; // bấm Hủy
 * </pre>
 * showDialog() trả về giá trị các ô theo thứ tự đã thêm. Ô combo trả về CHỈ SỐ được chọn (dạng chuỗi).
 */
public class FormDialog extends JDialog {
    private final JPanel content = new JPanel();
    private final List<JComponent> inputs = new ArrayList<>();
    private String[] result = null;

    public FormDialog(Component owner, String title) {
        super(SwingUtilities.getWindowAncestor(owner), title, ModalityType.APPLICATION_MODAL);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG);
        content.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.SERIF_TITLE.deriveFont(26f));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        setContentPane(content);
    }

    public FormDialog text(String label, String value) {
        JTextField field = new JTextField(value == null ? "" : value, 24);
        field.addActionListener(e -> submit()); // Enter = Lưu
        return addField(label, field);
    }

    public FormDialog password(String label) {
        JPasswordField field = new JPasswordField(24);
        field.addActionListener(e -> submit());
        return addField(label, field);
    }

    public FormDialog combo(String label, String[] options, int selected) {
        JComboBox<String> box = new JComboBox<>(options);
        if (selected >= 0 && selected < options.length) {
            box.setSelectedIndex(selected);
        }
        box.setFont(Theme.BODY);
        box.setAlignmentX(LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        content.add(Theme.fieldLabel(label));
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(box);
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        inputs.add(box);
        return this;
    }

    /** Nhúng một component tùy ý vào form (vd: bộ chọn thuộc tính); người gọi tự đọc dữ liệu từ component sau khi Lưu. */
    public FormDialog component(JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        content.add(component);
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        return this;
    }

    /** Hiện hộp thoại (chặn cho tới khi đóng). Trả về null nếu người dùng bấm Hủy. */
    public String[] showDialog() {
        NakedButton ok = new NakedButton("Lưu");
        ok.setPreferredSize(new Dimension(140, 42));
        ok.setMaximumSize(new Dimension(140, 42));
        ok.addActionListener(e -> submit());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.add(ok);
        row.add(Box.createRigidArea(new Dimension(20, 0)));
        row.add(new LinkLabel("Hủy", this::dispose));

        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(row);

        pack();
        setSize(Math.max(440, getWidth()), getHeight());
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return result;
    }

    private FormDialog addField(String label, JTextComponent field) {
        content.add(new FormField(label, field));
        inputs.add(field);
        return this;
    }

    private void submit() {
        String[] values = new String[inputs.size()];
        for (int i = 0; i < values.length; i++) {
            JComponent input = inputs.get(i);
            if (input instanceof JPasswordField) {
                values[i] = new String(((JPasswordField) input).getPassword());
            } else if (input instanceof JTextComponent) {
                values[i] = ((JTextComponent) input).getText().trim();
            } else {
                values[i] = String.valueOf(((JComboBox<?>) input).getSelectedIndex());
            }
        }
        result = values;
        dispose();
    }
}
