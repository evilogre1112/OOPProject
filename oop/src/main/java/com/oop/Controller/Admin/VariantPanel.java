package com.oop.Controller.Admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.PriceFormat;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Controller.Common.TableDialog;
import com.oop.Model.AttributeValue;
import com.oop.Model.Product;
import com.oop.Service.AccountService;
import com.oop.Service.ProductService;

/**
 * Bảng biến thể của nhóm đang chọn: thêm / sửa / xóa, chọn thuộc tính, quản lý
 * ảnh, xem tồn kho thấp.
 */
public class VariantPanel extends SectionPanel {
    private static class Data {
        List<Product> variants = new ArrayList<>();
        List<String> attributes = new ArrayList<>(); // chuỗi "Size: M, Màu: Đen" theo từng biến thể
    }

    /**
     * Tất cả thuộc tính đang có + thuộc tính hiện tại của một biến thể (để mở bộ
     * chọn).
     */
    private static class AttributeData {
        List<AttributeValue> all = new ArrayList<>();
        List<AttributeValue> current = new ArrayList<>();
    }

    private final AccountService accountService;
    private final DataTable table = new DataTable("Mã", "Thuộc tính", "Giá", "Tồn kho", "Trạng thái");
    private String groupId;
    private Data data = new Data();

    public VariantPanel(AccountService accountService) {
        super("Biến thể");
        this.accountService = accountService;

        bar().button("Thêm", this::onAdd);
        bar().button("Sửa", this::onEdit);
        bar().button("Xóa", this::onDelete);
        bar().button("Chọn thuộc tính", this::onAttributes);
        bar().button("Ảnh", this::onImages);
        bar().button("Tồn kho thấp", this::onLowStock);
        table.onDoubleClick(this::onAttributes); // bấm đúp một biến thể để chọn thuộc tính
        setBody(table);
    }

    /** Được GroupPanel gọi khi chọn nhóm khác (null = chưa chọn nhóm nào). */
    public void showGroup(String groupId) {
        this.groupId = groupId;
        reload();
    }

    private void reload() {
        String id = groupId;
        if (id == null) {
            data = new Data();
            table.setRows(new ArrayList<>());
            return;
        }
        Async.run(this, () -> load(id), loaded -> {
            if (!id.equals(groupId)) {
                return; // trong lúc chờ đã chọn nhóm khác
            }
            data = loaded;
            List<Object[]> rows = new ArrayList<>();
            for (int i = 0; i < data.variants.size(); i++) {
                Product p = data.variants.get(i);
                rows.add(new Object[] { p.getId(), data.attributes.get(i), PriceFormat.vnd(p.getPrice()),
                        p.getStockQuantity(), p.isActive() ? "Đang bán" : "Ngừng bán" });
            }
            table.setRows(rows);
        });
    }

    private Data load(String id) throws Exception {
        Data d = new Data();
        d.variants = ProductService.getVariants(id);
        for (Product p : d.variants) {
            StringBuilder text = new StringBuilder();
            for (AttributeValue a : ProductService.getAttributes(p.getId())) {
                if (text.length() > 0) {
                    text.append(", ");
                }
                text.append(a.getName()).append(": ").append(a.getValue());
            }
            d.attributes.add(text.toString());
        }
        return d;
    }

    // ===== Thêm / sửa / xóa biến thể =====

    /**
     * Tải toàn bộ thuộc tính có sẵn, rồi hiện form giá + tồn kho + bộ chọn thuộc
     * tính.
     */
    private void onAdd() {
        if (!hasGroup()) {
            return;
        }
        Async.run(this, ProductService::getAllAttributes, all -> {
            AttributePicker picker = new AttributePicker(all, new HashSet<>());
            String[] v = form("Thêm biến thể", null, picker);
            if (v == null) {
                return;
            }
            Product product = fromForm(v);
            List<AttributeValue> extra = product == null ? null : picker.parseNewAttributes();
            if (product == null || extra == null) {
                return; // dữ liệu nhập sai, đã báo lỗi
            }
            product.setGroupProductId(groupId);
            List<String> selectedIds = picker.getSelectedIds();
            Async.run(this, () -> addWithAttributes(product, selectedIds, extra), this::showResultAndReload);
        });
    }

    /**
     * Thêm biến thể rồi gắn thuộc tính đã chọn + thuộc tính nhập mới. Trả về null
     * nếu xong hết, ngược lại là lỗi.
     */
    private String addWithAttributes(Product product, List<String> selectedIds, List<AttributeValue> extra) {
        boolean added = false;
        try {
            if (!ProductService.addVariant(accountService, product)) {
                return "Không thể thêm biến thể, hãy kiểm tra lại dữ liệu.";
            }
            added = true;
            for (String id : selectedIds) {
                if (!ProductService.linkAttribute(product.getId(), id)) {
                    return "Đã thêm biến thể nhưng không gắn được một thuộc tính. Hãy bấm \"Chọn thuộc tính\" để thêm lại.";
                }
            }
            for (AttributeValue a : extra) {
                // dùng lại thuộc tính nếu đã có, chưa có thì thêm mới rồi gắn
                if (!ProductService.useAttribute(accountService, product.getId(), a.getName(), a.getValue())) {
                    return "Đã thêm biến thể nhưng thuộc tính " + a.getName() + "=" + a.getValue()
                            + " bị lỗi. Hãy bấm \"Chọn thuộc tính\" để thêm lại.";
                }
            }
            return null;
        } catch (SQLException e) {
            return (added ? "Đã thêm biến thể nhưng lỗi khi gắn thuộc tính: " : "Không thể thêm biến thể: ")
                    + e.getMessage();
        }
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Product old = data.variants.get(i);
        String[] v = form("Sửa biến thể", old, null);
        if (v == null) {
            return;
        }
        Product product = fromForm(v);
        if (product == null) {
            return;
        }
        product.setId(old.getId());
        product.setGroupProductId(old.getGroupProductId());
        Async.action(this, () -> ProductService.updateVariant(accountService, product), this::reload);
    }

    private void onDelete() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Product product = data.variants.get(i);
        if (!Dialogs.confirm(this, "Xóa biến thể này cùng ảnh của nó?")) {
            return;
        }
        Async.action(this, () -> ProductService.deleteVariant(accountService, product.getId()), this::reload);
    }

    // ===== Thuộc tính của biến thể =====

    /**
     * Mở bộ chọn: hiện toàn bộ thuộc tính khả thi, tích sẵn những cái biến thể đang
     * có.
     */
    private void onAttributes() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Product variant = data.variants.get(i);
        Async.run(this, () -> loadAttributes(variant.getId()), loaded -> {
            Set<String> current = new LinkedHashSet<>();
            for (AttributeValue a : loaded.current) {
                current.add(a.getId());
            }
            AttributePicker picker = new AttributePicker(loaded.all, current);
            if (new FormDialog(this, "Thuộc tính của biến thể").component(picker).showDialog() == null) {
                return;
            }
            List<AttributeValue> extra = picker.parseNewAttributes();
            if (extra == null) {
                return;
            }
            Set<String> selected = new LinkedHashSet<>(picker.getSelectedIds());
            Async.run(this, () -> applyAttributes(variant.getId(), current, selected, extra),
                    this::showResultAndReload);
        });
    }

    private AttributeData loadAttributes(String productId) throws Exception {
        AttributeData d = new AttributeData();
        d.all = ProductService.getAllAttributes();
        d.current = ProductService.getAttributes(productId);
        return d;
    }

    /**
     * Gỡ cái bỏ tích, gắn cái mới tích, rồi dùng các thuộc tính nhập mới. Trả về
     * null nếu xong hết, ngược lại là lỗi.
     */
    private String applyAttributes(String productId, Set<String> current, Set<String> selected,
            List<AttributeValue> extra) {
        try {
            // gỡ trước rồi mới gắn, để đổi Size M sang L không bị chặn vì trùng tên
            for (String id : current) {
                if (!selected.contains(id) && !ProductService.unlinkAttribute(productId, id)) {
                    return "Không gỡ được một thuộc tính khỏi biến thể.";
                }
            }
            for (String id : selected) {
                if (!current.contains(id) && !ProductService.linkAttribute(productId, id)) {
                    return "Không gắn được một thuộc tính vào biến thể.";
                }
            }
            for (AttributeValue a : extra) {
                if (!ProductService.useAttribute(accountService, productId, a.getName(), a.getValue())) {
                    return "Thuộc tính " + a.getName() + "=" + a.getValue() + " bị lỗi khi lưu.";
                }
            }
            return null;
        } catch (SQLException e) {
            return "Lỗi khi lưu thuộc tính: " + e.getMessage();
        }
    }

    private void showResultAndReload(String error) {
        if (error != null) {
            Dialogs.error(this, error);
        }
        reload();
    }

    // ===== Ảnh, tồn kho thấp =====

    private void onImages() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        new ImageDialog(this, accountService, data.variants.get(i)).setVisible(true);
    }

    private void onLowStock() {
        String[] v = new FormDialog(this, "Tồn kho thấp")
                .text("Cảnh báo khi tồn kho nhỏ hơn", "10")
                .showDialog();
        if (v == null) {
            return;
        }
        Integer threshold = Dialogs.integer(this, v[0], "Ngưỡng cảnh báo");
        if (threshold == null) {
            return;
        }
        Async.run(this, () -> ProductService.getLowStock(threshold), list -> {
            List<Object[]> rows = new ArrayList<>();
            for (Product p : list) {
                rows.add(new Object[] { p.getId(), p.getGroupProductId(), PriceFormat.vnd(p.getPrice()),
                        p.getStockQuantity(), p.isActive() ? "Đang bán" : "Ngừng bán" });
            }
            TableDialog.show(this, "Biến thể sắp hết hàng",
                    new String[] { "Mã biến thể", "Mã nhóm", "Giá", "Tồn kho", "Trạng thái" }, rows);
        });
    }

    // ===== Form =====

    private boolean hasGroup() {
        if (groupId == null) {
            Dialogs.info(this, "Hãy chọn một nhóm sản phẩm ở bảng phía trên.");
        }
        return groupId != null;
    }

    /**
     * picker khác null thì nhúng bộ chọn thuộc tính vào cuối form (dùng khi thêm
     * biến thể).
     */
    private String[] form(String title, Product p, AttributePicker picker) {
        FormDialog dialog = new FormDialog(this, title)
                .text("Giá (VND)", p == null ? "" : String.valueOf((long) p.getPrice()))
                .text("Tồn kho", p == null ? "" : String.valueOf(p.getStockQuantity()))
                .combo("Trạng thái", new String[] { "Đang bán", "Ngừng bán" }, p == null || p.isActive() ? 0 : 1);
        if (picker != null) {
            dialog.component(picker);
        }
        return dialog.showDialog();
    }

    /** Trả về null (và đã báo lỗi) nếu giá hoặc tồn kho không phải số. */
    private Product fromForm(String[] v) {
        Double price = Dialogs.number(this, v[0], "Giá");
        if (price == null) {
            return null;
        }
        Integer stock = Dialogs.integer(this, v[1], "Tồn kho");
        if (stock == null) {
            return null;
        }
        Product product = new Product();
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setActive("0".equals(v[2]));
        return product;
    }
}