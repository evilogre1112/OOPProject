package com.oop.Controller.Admin;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JTextField;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.PriceFormat;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Brand;
import com.oop.Model.Category;
import com.oop.Model.GroupProduct;
import com.oop.Service.AccountService;
import com.oop.Service.CatalogService;
import com.oop.Service.ProductService;

/** Bảng nhóm sản phẩm: tìm theo tên, thêm / sửa / xóa. Chọn một dòng thì báo cho onSelect (id nhóm). */
public class GroupPanel extends SectionPanel {
    /** Dữ liệu tải một lượt từ DB. */
    private static class Data {
        List<GroupProduct> groups = new ArrayList<>();
        Map<String, Double> prices = new HashMap<>();
        List<String> categoryIds = new ArrayList<>();
        List<String> categoryNames = new ArrayList<>();
        List<String> brandIds = new ArrayList<>();
        List<String> brandNames = new ArrayList<>();
    }

    private final AccountService accountService;
    private final Consumer<String> onSelect;
    private final DataTable table = new DataTable("Tên", "Danh mục", "Thương hiệu", "Giá thấp nhất", "Mô tả");
    private final JTextField txtSearch = new JTextField();
    private Data data = new Data();

    public GroupPanel(AccountService accountService, Consumer<String> onSelect) {
        super("Sản phẩm");
        this.accountService = accountService;
        this.onSelect = onSelect;

        Theme.styleField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(220, 38));
        txtSearch.setToolTipText("Tìm theo tên sản phẩm");
        txtSearch.addActionListener(e -> reload());

        bar().addField(txtSearch);
        bar().button("Tìm", this::reload);
        bar().button("Thêm", this::onAdd);
        bar().button("Sửa", this::onEdit);
        bar().button("Xóa", this::onDelete);

        table.onSelectionChanged(() -> {
            int i = table.getSelectedIndex();
            onSelect.accept(i < 0 || i >= data.groups.size() ? null : data.groups.get(i).getId());
        });
        setBody(table);
        reload();
    }

    private void reload() {
        String keyword = txtSearch.getText().trim();
        Async.run(this, () -> load(keyword), loaded -> {
            data = loaded;
            List<Object[]> rows = new ArrayList<>();
            for (GroupProduct g : data.groups) {
                rows.add(new Object[] {
                        g.getName(),
                        nameOf(data.categoryIds, data.categoryNames, g.getCategoryId()),
                        nameOf(data.brandIds, data.brandNames, g.getBrandId()),
                        PriceFormat.vnd(data.prices.getOrDefault(g.getId(), 0.0)),
                        g.getDescription() });
            }
            table.setRows(rows);
        });
    }

    private Data load(String keyword) throws Exception {
        Data d = new Data();
        d.groups = ProductService.search(null, null, null, null, keyword.isEmpty() ? null : keyword, "newest");
        for (Category c : CatalogService.getAllCategories()) {
            d.categoryIds.add(c.getId());
            d.categoryNames.add(c.getName());
        }
        for (Brand b : CatalogService.getAllBrands()) {
            d.brandIds.add(b.getId());
            d.brandNames.add(b.getName());
        }
        for (GroupProduct g : d.groups) {
            d.prices.put(g.getId(), ProductService.getMinPrice(g.getId()));
        }
        return d;
    }

    private void onAdd() {
        if (data.categoryIds.isEmpty() || data.brandIds.isEmpty()) {
            Dialogs.info(this, "Hãy tạo danh mục và thương hiệu trước khi thêm sản phẩm.");
            return;
        }
        String[] v = form("Thêm nhóm sản phẩm", null);
        if (v == null) {
            return;
        }
        GroupProduct group = fromForm(v);
        Async.action(this, () -> ProductService.addGroup(accountService, group), this::reload);
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        GroupProduct old = data.groups.get(i);
        String[] v = form("Sửa nhóm sản phẩm", old);
        if (v == null) {
            return;
        }
        GroupProduct group = fromForm(v);
        group.setId(old.getId());
        Async.action(this, () -> ProductService.updateGroup(accountService, group), this::reload);
    }

    private void onDelete() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        GroupProduct group = data.groups.get(i);
        if (!Dialogs.confirm(this, "Xóa \"" + group.getName() + "\" cùng toàn bộ biến thể và ảnh của nó?")) {
            return;
        }
        Async.action(this, () -> ProductService.deleteGroup(accountService, group.getId()), this::reload);
    }

    private String[] form(String title, GroupProduct g) {
        return new FormDialog(this, title)
                .text("Tên", g == null ? "" : g.getName())
                .text("Mô tả", g == null || g.getDescription() == null ? "" : g.getDescription())
                .combo("Danh mục", data.categoryNames.toArray(new String[0]),
                        g == null ? 0 : data.categoryIds.indexOf(g.getCategoryId()))
                .combo("Thương hiệu", data.brandNames.toArray(new String[0]),
                        g == null ? 0 : data.brandIds.indexOf(g.getBrandId()))
                .showDialog();
    }

    private GroupProduct fromForm(String[] v) {
        GroupProduct group = new GroupProduct();
        group.setName(v[0]);
        group.setDescription(v[1]);
        group.setCategoryId(data.categoryIds.get(Integer.parseInt(v[2])));
        group.setBrandId(data.brandIds.get(Integer.parseInt(v[3])));
        return group;
    }

    private static String nameOf(List<String> ids, List<String> names, String id) {
        int index = ids.indexOf(id);
        return index < 0 ? "" : names.get(index);
    }
}
