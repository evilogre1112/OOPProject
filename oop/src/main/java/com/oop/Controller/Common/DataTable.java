package com.oop.Controller.Common;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Bảng dữ liệu theo phong cách NAKED: chỉ có đường kẻ ngang mảnh, header chữ
 * hoa giãn chữ, chọn một dòng.
 */
public class DataTable extends JScrollPane {
    private static final Color SELECTED = new Color(0xEADCE1); // đỏ đô rất nhạt

    private final DefaultTableModel model;
    private final JTable table;

    public DataTable(String... columns) {
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.LINE);
        table.setFont(Theme.BODY);
        table.setBackground(Theme.BG);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setReorderingAllowed(false);

        setViewportView(table);
        setColumnHeaderView(table.getTableHeader());
        getColumnHeader().setBackground(Theme.BG);
        getViewport().setBackground(Theme.BG);
        setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.LINE));
    }

    /**
     * Thay toàn bộ các dòng (thứ tự dòng trùng với thứ tự list dữ liệu của người
     * gọi).
     */
    public void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    /** Đổi cả tên cột lẫn dữ liệu (dùng khi một bảng hiện nhiều loại dữ liệu). */
    public void setData(String[] columns, List<Object[]> rows) {
        model.setColumnIdentifiers(columns);
        setRows(rows);
    }

    /** Chỉ số dòng đang chọn, -1 nếu chưa chọn. */
    public int getSelectedIndex() {
        return table.getSelectedRow();
    }

    /** Trả về dòng đang chọn; chưa chọn thì báo cho người dùng và trả về -1. */
    public int requireSelection() {
        int index = table.getSelectedRow();
        if (index < 0) {
            Dialogs.info(this, "Hãy chọn một dòng trong bảng trước.");
        }
        return index;
    }

    /** Bấm đúp vào một dòng thì chạy listener (dòng đó đã được chọn). */
    public void onDoubleClick(Runnable listener) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    listener.run();
                }
            }
        });
    }

    public void onSelectionChanged(Runnable listener) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                listener.run();
            }
        });
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, column);
            label.setBackground(selected ? SELECTED : Theme.BG);
            label.setForeground(Theme.INK);
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return label;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int column) {
            String text = value == null ? "" : value.toString().toUpperCase();
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, text, false, false, row, column);
            label.setFont(Theme.spaced(Theme.LABEL, 0.12f));
            label.setForeground(Theme.MUTED);
            label.setBackground(Theme.BG);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            return label;
        }
    }
}