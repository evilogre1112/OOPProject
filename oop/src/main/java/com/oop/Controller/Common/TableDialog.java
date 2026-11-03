package com.oop.Controller.Common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Hộp thoại chỉ để xem một bảng dữ liệu (vd: danh sách biến thể sắp hết hàng).
 */
public final class TableDialog {
    private TableDialog() {
    }

    public static void show(Component owner, String title, String[] columns, List<Object[]> rows) {
        DataTable table = new DataTable(columns);
        table.setRows(rows);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.add(table, BorderLayout.CENTER);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(owner), title,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(panel);
        dialog.setSize(760, 440);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
