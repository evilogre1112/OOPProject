package com.oop.Controller.Admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
 * Bảng biến thể của nhóm đang chọn: thêm / sửa / xóa, quản lý thuộc tính, ảnh,
 * xem tồn kho thấp.
 */
public class VariantPanel extends SectionPanel {
    private static class Data {
        List<Product> variants = new ArrayList<>();
        List<String> attributes = new ArrayList<>(); // chuỗi "Size: M, Màu: Đen" theo từng biến thể
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
        bar().button("Sửa thuộc tính", this::onAttributes);
        bar().button("Ảnh", this::onImages);
        bar().button("Tồn kho thấp", this::onLowStock);
        table.onDoubleClick(this::onAttributes); // bấm đúp một biến thể để thêm / sửa / xóa thuộc tính
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

    private void onAdd() {
        if (!hasGroup()) {
            return;
        }
        String[] v = form("Thêm biến thể", null);
        if (v == null) {
            return;
        }
        Product product = fromForm(v);
        List<AttributeValue> attributes = product == null ? null : parseAttributes(v[3]);
        if (product == null || attributes == null) {
            return; // dữ liệu nhập sai, đã báo lỗi
        }
        product.setGroupProductId(groupId);
        Async.run(this, () -> addWithAttributes(product, attributes), error -> {
            if (error != null) {
                Dialogs.error(this, error);
            }
            reload();
        });
    }

    /**
     * Thêm biến thể rồi thêm + gắn từng thuộc tính. Trả về null nếu xong hết, ngược
     * lại là thông báo lỗi.
     */
    private String addWithAttributes(Product product, List<AttributeValue> attributes) {
        boolean added = false;
        try {
            if (!ProductService.addVariant(accountService, product)) {
                return "Không thể thêm biến thể, hãy kiểm tra lại dữ liệu.";
            }
            added = true;
            for (AttributeValue a : attributes) {
                boolean ok = ProductService.addAttributeValue(accountService, a)
                        && ProductService.linkAttribute(product.getId(), a.getId());
                if (!ok) {
                    return "Đã thêm biến thể nhưng thuộc tính " + a.getName() + "=" + a.getValue()
                            + " bị lỗi. Hãy chọn biến thể rồi bấm \"Sửa thuộc tính\" để thêm lại.";
                }
            }
            return null;
        } catch (SQLException e) {
            return (added ? "Đã thêm biến thể nhưng lỗi khi thêm thuộc tính: " : "Không thể thêm biến thể: ")
                    + e.getMessage();
        }
    }

    /**
     * "Size=M, Màu=Đen" thành danh sách thuộc tính. Để trống thì được danh sách
     * rỗng; sai dạng thì báo lỗi và trả về null.
     */
    private List<AttributeValue> parseAttributes(String text) {
        List<AttributeValue> list = new ArrayList<>();
        if (text.isEmpty()) {
            return list;
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
            list.add(attr);
        }
        return list;
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Product old = data.variants.get(i);
        String[] v = form("Sửa biến thể", old);
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
        if (!Dialogs.confirm(this, "Xóa biến thể này cùng ảnh và thuộc tính của nó?")) {
            return;
        }
        Async.action(this, () -> ProductService.deleteVariant(accountService, product.getId()), this::reload);
    }

    private void onAttributes() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        new AttributeDialog(this, accountService, data.variants.get(i)).setVisible(true);
        reload();
    }

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

    private boolean hasGroup() {
        if (groupId == null) {
            Dialogs.info(this, "Hãy chọn một nhóm sản phẩm ở bảng phía trên.");
        }
        return groupId != null;
    }

    /**
     * Form thêm có thêm ô thuộc tính (v[3]); form sửa thì không, vì thuộc tính sửa
     * riêng ở "Sửa thuộc tính".
     */
    private String[] form(String title, Product p) {
        FormDialog dialog = new FormDialog(this, title)
                .text("Giá (VND)", p == null ? "" : String.valueOf((long) p.getPrice()))
                .text("Tồn kho", p == null ? "" : String.valueOf(p.getStockQuantity()))
                .combo("Trạng thái", new String[] { "Đang bán", "Ngừng bán" }, p == null || p.isActive() ? 0 : 1);
        if (p == null) {
            dialog.text("Thuộc tính (vd: Size=M, Màu=Đen)", "");
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