package com.oop.Controller.Admin;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormField;
import com.oop.Controller.Common.ScrollablePanel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.AttributeValue;

/**
 * Bộ chọn thuộc tính cho một biến thể: hiện TOÀN BỘ thuộc tính đang có (nhóm
 * theo tên, mỗi giá trị một ô chọn)
 * và một ô để nhập thêm thuộc tính mới dạng "Tên=Giá trị". Nhúng vào FormDialog
 * bằng component().
 */
public class AttributePicker extends JPanel {
    private final Map<String, JCheckBox> boxes = new LinkedHashMap<>(); // id thuộc tính -> ô chọn
    private final Map<String, AttributeValue> byId = new LinkedHashMap<>(); // id thuộc tính -> thuộc tính
    private final JTextField txtNew = new JTextField();

    /**
     * all: mọi thuộc tính đang có (đã sắp theo tên); selectedIds: các id được tích
     * sẵn.
     */
    public AttributePicker(List<AttributeValue> all, Set<String> selectedIds) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JLabel label = Theme.fieldLabel("Chọn thuộc tính có sẵn (mỗi thuộc tính một giá trị)");
        add(label);
        add(Box.createRigidArea(new Dimension(0, 6)));
        add(createList(all, selectedIds));
        add(Box.createRigidArea(new Dimension(0, 12)));

        FormField newField = new FormField("Hoặc thêm mới (Tên=Giá trị, cách nhau bằng dấu phẩy)", txtNew);
        add(newField);
    }

    private JScrollPane createList(List<AttributeValue> all, Set<String> selectedIds) {
        ScrollablePanel list = new ScrollablePanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.BG);
        list.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        if (all.isEmpty()) {
            JLabel empty = new JLabel("Chưa có thuộc tính nào, hãy thêm mới ở ô bên dưới.");
            empty.setFont(Theme.SMALL);
            empty.setForeground(Theme.MUTED);
            list.add(empty);
        }

        // Nhóm theo tên thuộc tính, giữ thứ tự đã sắp
        Map<String, List<AttributeValue>> byName = new LinkedHashMap<>();
        for (AttributeValue a : all) {
            byName.computeIfAbsent(a.getName(), k -> new ArrayList<>()).add(a);
        }
        for (Map.Entry<String, List<AttributeValue>> group : byName.entrySet()) {
            JLabel name = Theme.fieldLabel(group.getKey());
            list.add(name);
            list.add(Box.createRigidArea(new Dimension(0, 4)));

            JPanel row = new JPanel(new GridLayout(0, 4, 8, 2));
            row.setOpaque(false);
            row.setAlignmentX(LEFT_ALIGNMENT);
            List<JCheckBox> sameName = new ArrayList<>(); // các ô của cùng một tên thuộc tính
            boolean alreadyTicked = false;
            for (AttributeValue a : group.getValue()) {
                // dữ liệu cũ lỡ có hai giá trị cùng tên thì chỉ tích sẵn giá trị đầu tiên
                boolean tick = selectedIds.contains(a.getId()) && !alreadyTicked;
                alreadyTicked |= tick;

                JCheckBox box = new JCheckBox(a.getValue(), tick);
                box.setOpaque(false);
                box.setFont(Theme.BODY);
                box.setForeground(Theme.INK);
                box.addActionListener(e -> {
                    if (box.isSelected()) { // chọn giá trị này thì bỏ các giá trị khác cùng tên
                        for (JCheckBox other : sameName) {
                            if (other != box) {
                                other.setSelected(false);
                            }
                        }
                    }
                });
                sameName.add(box);
                boxes.put(a.getId(), box);
                byId.put(a.getId(), a);
                row.add(box);
            }
            list.add(row);
            list.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Theme.LINE));
        scroll.getViewport().setBackground(Theme.BG);
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(560, 200));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return scroll;
    }

    /** Id các thuộc tính có sẵn đang được tích chọn. */
    public List<String> getSelectedIds() {
        List<String> ids = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> e : boxes.entrySet()) {
            if (e.getValue().isSelected()) {
                ids.add(e.getKey());
            }
        }
        return ids;
    }

    /**
     * Đọc ô thêm mới ("Size=M, Màu=Đen") thành danh sách thuộc tính. Để trống thì
     * trả về danh sách rỗng;
     * sai dạng thì báo lỗi và trả về null. Thuộc tính trùng với thuộc tính có sẵn
     * sẽ được dùng lại khi lưu.
     */
    public List<AttributeValue> parseNewAttributes() {
        List<AttributeValue> list = new ArrayList<>();
        String text = txtNew.getText().trim();
        if (text.isEmpty()) {
            return list;
        }

        // tên thuộc tính (viết thường) -> giá trị đã được chọn; mỗi tên chỉ được một
        // giá trị
        Map<String, String> chosen = new LinkedHashMap<>();
        for (String id : getSelectedIds()) {
            AttributeValue a = byId.get(id);
            chosen.put(a.getName().toLowerCase(), a.getValue().toLowerCase());
        }

        for (String part : text.split("[,;]")) {
            String[] pair = part.split("=", 2);
            if (pair.length < 2 || pair[0].trim().isEmpty() || pair[1].trim().isEmpty()) {
                Dialogs.error(this, "Thuộc tính \"" + part.trim() + "\" sai dạng. Hãy nhập Tên=Giá trị, "
                        + "cách nhau bằng dấu phẩy, ví dụ: Size=M, Màu=Đen");
                return null;
            }
            AttributeValue attr = new AttributeValue();
            attr.setName(pair[0].trim());
            attr.setValue(pair[1].trim());

            String key = attr.getName().toLowerCase();
            String existing = chosen.get(key);
            if (existing != null) {
                if (existing.equals(attr.getValue().toLowerCase())) {
                    continue; // trùng đúng giá trị đã chọn, bỏ qua
                }
                Dialogs.error(this, "Thuộc tính \"" + attr.getName() + "\" chỉ được một giá trị, "
                        + "nhưng bạn đã chọn hoặc nhập hai giá trị khác nhau.");
                return null;
            }
            chosen.put(key, attr.getValue().toLowerCase());
            list.add(attr);
        }
        return list;
    }
}