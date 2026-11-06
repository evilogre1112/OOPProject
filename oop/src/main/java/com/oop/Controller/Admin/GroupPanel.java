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

/**
 * Bảng nhóm sản phẩm: tìm theo tên, thêm / sửa / xóa. Chọn một dòng thì báo cho
 * onSelect (id nhóm).
 * Danh mục và thương hiệu: chọn cái có sẵn, hoặc nhập tên mới (đã có thì dùng
 * lại, chưa có thì tự thêm).
 */
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
        String[] v = form("Thêm nhóm sản phẩm", null);
        if (v == null || !valid(v)) {
            return;
        }
        save(v, null);
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        GroupProduct old = data.groups.get(i);
        String[] v = form("Sửa nhóm sản phẩm", old);
        if (v == null || !valid(v)) {
            return;
        }
        save(v, old.getId());
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

    /**
     * Lưu nhóm sản phẩm (id null = thêm mới). Danh mục / thương hiệu nhập mới thì
     * dùng useCategory / useBrand:
     * đã có cùng tên thì dùng lại, chưa có thì thêm mới rồi dùng.
     */
    private void save(String[] v, String groupId) {
        // đọc dữ liệu form trên EDT trước khi chuyển sang luồng nền
        String categoryId = comboIndex(v[2]) > 0 ? data.categoryIds.get(comboIndex(v[2]) - 1) : null;
        String brandId = comboIndex(v[4]) > 0 ? data.brandIds.get(comboIndex(v[4]) - 1) : null;
        String newCategory = v[3];
        String newBrand = v[5];

        Async.action(this, () -> {
            GroupProduct group = new GroupProduct();
            group.setName(v[0]);
            group.setDescription(v[1]);
            group.setCategoryId(newCategory.isEmpty() ? categoryId
                    : CatalogService.useCategory(newCategory, accountService).getId());
            group.setBrandId(newBrand.isEmpty() ? brandId
                    : CatalogService.useBrand(newBrand, null, accountService).getId());
            if (groupId == null) {
                return ProductService.addGroup(accountService, group);
            }
            group.setId(groupId);
            return ProductService.updateGroup(accountService, group);
        }, this::reload);
    }

    /**
     * Form: v[0] tên, v[1] mô tả, v[2] danh mục có sẵn, v[3] danh mục mới, v[4]
     * thương hiệu có sẵn, v[5] thương hiệu mới.
     */
    private String[] form(String title, GroupProduct g) {
        return new FormDialog(this, title)
                .text("Tên", g == null ? "" : g.getName())
                .text("Mô tả", g == null || g.getDescription() == null ? "" : g.getDescription())
                .combo("Danh mục", withPlaceholder("(chọn danh mục có sẵn)", data.categoryNames),
                        g == null ? 0 : data.categoryIds.indexOf(g.getCategoryId()) + 1)
                .text("Hoặc nhập danh mục mới (đã có tên này thì dùng lại)", "")
                .combo("Thương hiệu", withPlaceholder("(chọn thương hiệu có sẵn)", data.brandNames),
                        g == null ? 0 : data.brandIds.indexOf(g.getBrandId()) + 1)
                .text("Hoặc nhập thương hiệu mới (đã có tên này thì dùng lại)", "")
                .showDialog();
    }

    /**
     * Phải có tên, và với danh mục / thương hiệu: chọn một cái có sẵn hoặc nhập tên
     * mới.
     */
    private boolean valid(String[] v) {
        if (v[0].isEmpty()) {
            Dialogs.error(this, "Tên sản phẩm không được để trống");
            return false;
        }
        if (v[3].isEmpty() && comboIndex(v[2]) == 0) {
            Dialogs.error(this, "Hãy chọn một danh mục có sẵn hoặc nhập danh mục mới");
            return false;
        }
        if (v[5].isEmpty() && comboIndex(v[4]) == 0) {
            Dialogs.error(this, "Hãy chọn một thương hiệu có sẵn hoặc nhập thương hiệu mới");
            return false;
        }
        return true;
    }

    /**
     * Ô combo trong FormDialog trả về chỉ số dạng chuỗi; 0 là dòng "(chọn ...)".
     */
    private static int comboIndex(String value) {
        return Integer.parseInt(value);
    }

    private static String[] withPlaceholder(String placeholder, List<String> names) {
        List<String> options = new ArrayList<>();
        options.add(placeholder);
        options.addAll(names);
        return options.toArray(new String[0]);
    }

    private static String nameOf(List<String> ids, List<String> names, String id) {
        int index = ids.indexOf(id);
        return index < 0 ? "" : names.get(index);
    }
}