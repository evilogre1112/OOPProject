package com.oop.Controller.Admin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Model.Category;
import com.oop.Service.AccountService;
import com.oop.Service.CatalogService;

/** Trang Danh mục: thêm / đổi tên / xóa. */
public class CategoryPanel extends SectionPanel {
    private final AccountService accountService;
    private final DataTable table = new DataTable("Tên danh mục", "Mã");
    private List<Category> categories = new ArrayList<>();

    public CategoryPanel(AccountService accountService) {
        super("Danh mục");
        this.accountService = accountService;

        bar().button("Thêm", this::onAdd);
        bar().button("Đổi tên", this::onRename);
        bar().button("Xóa", this::onDelete);
        setBody(table);
        reload();
    }

    private void reload() {
        Async.run(this, CatalogService::getAllCategories, list -> {
            categories = list;
            List<Object[]> rows = new ArrayList<>();
            for (Category c : list) {
                rows.add(new Object[] { c.getName(), c.getId() });
            }
            table.setRows(rows);
        });
    }

    private void onAdd() {
        String[] v = new FormDialog(this, "Thêm danh mục").text("Tên danh mục", "").showDialog();
        if (v == null) {
            return;
        }
        if (v[0].isEmpty()) {
            Dialogs.error(this, "Tên danh mục không được để trống");
            return;
        }
        Category category = new Category(UUID.randomUUID().toString(), v[0]);
        Async.action(this, () -> CatalogService.addCategory(category, accountService), this::reload);
    }

    private void onRename() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Category category = categories.get(i);
        String[] v = new FormDialog(this, "Đổi tên danh mục").text("Tên danh mục", category.getName()).showDialog();
        if (v == null) {
            return;
        }
        if (v[0].isEmpty()) {
            Dialogs.error(this, "Tên danh mục không được để trống");
            return;
        }
        Async.action(this, () -> CatalogService.updateCategory(category, v[0], accountService), this::reload);
    }

    private void onDelete() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Category category = categories.get(i);
        if (!Dialogs.confirm(this, "Xóa danh mục \"" + category.getName() + "\"? (bị chặn nếu còn sản phẩm thuộc danh mục này)")) {
            return;
        }
        Async.action(this, () -> CatalogService.deleteCategory(category.getId(), accountService), this::reload);
    }
}
