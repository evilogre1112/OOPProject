package com.oop.Controller.Admin;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Model.AttributeValue;
import com.oop.Model.Product;
import com.oop.Service.AccountService;
import com.oop.Service.ProductService;

/** Quản lý thuộc tính (Size, Màu...) của một biến thể: thêm mới + gắn, sửa, gỡ khỏi biến thể, xóa hẳn. */
public class AttributeDialog extends JDialog {
    private final AccountService accountService;
    private final Product variant;
    private final DataTable table = new DataTable("Thuộc tính", "Giá trị");
    private List<AttributeValue> attributes = new ArrayList<>();

    public AttributeDialog(Component owner, AccountService accountService, Product variant) {
        super(SwingUtilities.getWindowAncestor(owner), "Thuộc tính của biến thể", ModalityType.APPLICATION_MODAL);
        this.accountService = accountService;
        this.variant = variant;

        SectionPanel section = new SectionPanel("Thuộc tính");
        section.bar().button("Thêm", this::onAdd);
        section.bar().button("Sửa", this::onEdit);
        section.bar().button("Gỡ khỏi biến thể", this::onUnlink);
        section.bar().button("Xóa hẳn", this::onDelete);
        section.setBody(table);

        setContentPane(section);
        setSize(780, 480);
        setLocationRelativeTo(owner);
        reload();
    }

    private void reload() {
        Async.run(this, () -> ProductService.getAttributes(variant.getId()), list -> {
            attributes = list;
            List<Object[]> rows = new ArrayList<>();
            for (AttributeValue a : list) {
                rows.add(new Object[] { a.getName(), a.getValue() });
            }
            table.setRows(rows);
        });
    }

    private void onAdd() {
        String[] v = new FormDialog(this, "Thêm thuộc tính")
                .text("Tên thuộc tính (vd: Size, Màu)", "Size")
                .text("Giá trị (vd: M, Đen)", "")
                .showDialog();
        if (v == null) {
            return;
        }
        AttributeValue attr = new AttributeValue();
        attr.setName(v[0]);
        attr.setValue(v[1]);
        // addAttributeValue tự sinh id cho attr, sau đó gắn vào biến thể
        Async.action(this, () -> ProductService.addAttributeValue(accountService, attr)
                && ProductService.linkAttribute(variant.getId(), attr.getId()), this::reload);
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        AttributeValue old = attributes.get(i);
        String[] v = new FormDialog(this, "Sửa thuộc tính")
                .text("Tên thuộc tính", old.getName())
                .text("Giá trị", old.getValue())
                .showDialog();
        if (v == null) {
            return;
        }
        AttributeValue attr = new AttributeValue();
        attr.setId(old.getId());
        attr.setName(v[0]);
        attr.setValue(v[1]);
        Async.action(this, () -> ProductService.updateAttributeValue(accountService, attr), this::reload);
    }

    private void onUnlink() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        AttributeValue attr = attributes.get(i);
        Async.action(this, () -> ProductService.unlinkAttribute(variant.getId(), attr.getId()), this::reload);
    }

    private void onDelete() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        AttributeValue attr = attributes.get(i);
        if (!Dialogs.confirm(this, "Xóa hẳn giá trị này khỏi MỌI biến thể đang dùng chung nó?")) {
            return;
        }
        Async.action(this, () -> ProductService.deleteAttributeValue(accountService, attr.getId()), this::reload);
    }
}
