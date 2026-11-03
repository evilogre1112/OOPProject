package com.oop.Controller.Admin;

import java.util.ArrayList;
import java.util.List;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Model.Brand;
import com.oop.Service.AccountService;
import com.oop.Service.CatalogService;

/** Trang Thương hiệu: thêm / sửa (tên + logo) / xóa. */
public class BrandPanel extends SectionPanel {
    private final AccountService accountService;
    private final DataTable table = new DataTable("Tên thương hiệu", "Logo", "Mã");
    private List<Brand> brands = new ArrayList<>();

    public BrandPanel(AccountService accountService) {
        super("Thương hiệu");
        this.accountService = accountService;

        bar().button("Thêm", this::onAdd);
        bar().button("Sửa", this::onEdit);
        bar().button("Xóa", this::onDelete);
        setBody(table);
        reload();
    }

    private void reload() {
        Async.run(this, CatalogService::getAllBrands, list -> {
            brands = list;
            List<Object[]> rows = new ArrayList<>();
            for (Brand b : list) {
                rows.add(new Object[] { b.getName(), b.getLogo(), b.getId() });
            }
            table.setRows(rows);
        });
    }

    private void onAdd() {
        String[] v = form("Thêm thương hiệu", null);
        if (v == null) {
            return;
        }
        if (v[0].isEmpty()) {
            Dialogs.error(this, "Tên thương hiệu không được để trống");
            return;
        }
        Brand brand = new Brand(null, v[0], v[1].isEmpty() ? null : v[1]); // id do CatalogService.addBrand tự sinh
        Async.action(this, () -> CatalogService.addBrand(brand, accountService), this::reload);
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Brand brand = brands.get(i);
        String[] v = form("Sửa thương hiệu", brand);
        if (v == null) {
            return;
        }
        if (v[0].isEmpty()) {
            Dialogs.error(this, "Tên thương hiệu không được để trống");
            return;
        }
        String logo = v[1].isEmpty() ? null : v[1];
        Async.action(this, () -> CatalogService.updateBrand(brand, v[0], logo, accountService), this::reload);
    }

    private void onDelete() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Brand brand = brands.get(i);
        if (!Dialogs.confirm(this, "Xóa thương hiệu \"" + brand.getName() + "\"? (bị chặn nếu còn sản phẩm thuộc thương hiệu này)")) {
            return;
        }
        Async.action(this, () -> CatalogService.deleteBrand(brand.getId(), accountService), this::reload);
    }

    private String[] form(String title, Brand b) {
        return new FormDialog(this, title)
                .text("Tên thương hiệu", b == null ? "" : b.getName())
                .text("Logo (đường dẫn ảnh, có thể bỏ trống)", b == null || b.getLogo() == null ? "" : b.getLogo())
                .showDialog();
    }
}
