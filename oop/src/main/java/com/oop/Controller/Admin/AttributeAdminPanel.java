package com.oop.Controller.Admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Model.AttributeValue;
import com.oop.Service.AccountService;
import com.oop.Service.ProductService;

/** Trang Thuộc tính: xem mọi thuộc tính đang có (Size, Màu...), thêm / sửa / xóa. */
public class AttributeAdminPanel extends SectionPanel {
    private static class Data {
        List<AttributeValue> attributes = new ArrayList<>();
        Map<String, Integer> usage = new HashMap<>();
    }

    private final AccountService accountService;
    private final DataTable table = new DataTable("Thuộc tính", "Giá trị", "Số biến thể đang dùng", "Mã");
    private Data data = new Data();

    public AttributeAdminPanel(AccountService accountService) {
        super("Thuộc tính");
        this.accountService = accountService;

        bar().button("Thêm", this::onAdd);
        bar().button("Sửa", this::onEdit);
        bar().button("Xóa", this::onDelete);
        setBody(table);
        reload();
    }

    private void reload() {
        Async.run(this, this::load, loaded -> {
            data = loaded;
            List<Object[]> rows = new ArrayList<>();
            for (AttributeValue a : data.attributes) {
                rows.add(new Object[] { a.getName(), a.getValue(), data.usage.getOrDefault(a.getId(), 0), a.getId() });
            }
            table.setRows(rows);
        });
    }

    private Data load() throws Exception {
        Data d = new Data();
        d.attributes = ProductService.getAllAttributes();
        d.usage = ProductService.getAttributeUsage();
        return d;
    }

    private void onAdd() {
        String[] v = form("Thêm thuộc tính", null);
        if (v == null) {
            return;
        }
        AttributeValue attr = fromForm(v);
        if (attr == null) {
            return;
        }
        // trùng name + value với thuộc tính có sẵn thì service báo lỗi "đã tồn tại"
        Async.action(this, () -> ProductService.addAttributeValue(accountService, attr), this::reload);
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        AttributeValue old = data.attributes.get(i);
        String[] v = form("Sửa thuộc tính", old);
        if (v == null) {
            return;
        }
        AttributeValue attr = fromForm(v);
        if (attr == null) {
            return;
        }
        attr.setId(old.getId());
        Async.action(this, () -> ProductService.updateAttributeValue(accountService, attr), this::reload);
    }

    private void onDelete() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        AttributeValue attr = data.attributes.get(i);
        int used = data.usage.getOrDefault(attr.getId(), 0);
        String message = "Xóa thuộc tính " + attr.getName() + " = " + attr.getValue() + "?";
        if (used > 0) {
            message += "\nĐang có " + used + " biến thể dùng thuộc tính này, nó sẽ bị gỡ khỏi các biến thể đó.";
        }
        if (!Dialogs.confirm(this, message)) {
            return;
        }
        Async.action(this, () -> ProductService.deleteAttributeValue(accountService, attr.getId()), this::reload);
    }

    private String[] form(String title, AttributeValue a) {
        return new FormDialog(this, title)
                .text("Tên thuộc tính (vd: Size, Màu)", a == null ? "" : a.getName())
                .text("Giá trị (vd: M, Đen)", a == null ? "" : a.getValue())
                .showDialog();
    }

    private AttributeValue fromForm(String[] v) {
        if (v[0].isEmpty() || v[1].isEmpty()) {
            Dialogs.error(this, "Tên thuộc tính và giá trị không được để trống");
            return null;
        }
        AttributeValue attr = new AttributeValue();
        attr.setName(v[0]);
        attr.setValue(v[1]);
        return attr;
    }
}
