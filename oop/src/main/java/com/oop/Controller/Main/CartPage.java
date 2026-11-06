package com.oop.Controller.Main;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.PriceFormat;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;
import com.oop.Model.Address;
import com.oop.Model.OrderDetail;
import com.oop.Service.AddressService;
import com.oop.Service.CartService;
import com.oop.Service.OrderService;

/** Giỏ hàng: đổi số lượng, xóa, thanh toán (chọn địa chỉ giao hàng rồi đặt đơn). */
public class CartPage extends SectionPanel {
    private final Account account;
    private final Runnable onCartChanged; // cập nhật số trên header
    private final Runnable onNeedAddress; // chưa có địa chỉ -> chuyển sang sổ địa chỉ
    private final Runnable onOrdered; // đặt xong -> chuyển sang đơn hàng

    private final DataTable table = new DataTable("Sản phẩm", "Đơn giá", "Số lượng", "Thành tiền");
    private final JLabel lblTotal = new JLabel(" ", SwingConstants.RIGHT);
    private List<OrderDetail> items = new ArrayList<>();

    public CartPage(Account account, Runnable onCartChanged, Runnable onNeedAddress, Runnable onOrdered) {
        super("Giỏ hàng");
        this.account = account;
        this.onCartChanged = onCartChanged;
        this.onNeedAddress = onNeedAddress;
        this.onOrdered = onOrdered;

        bar().button("Đổi số lượng", this::onChangeQuantity);
        bar().button("Xóa", this::onRemove);
        bar().button("Xóa hết", this::onClear);
        bar().button("Thanh toán", this::onCheckout);

        lblTotal.setFont(Theme.SERIF_TITLE.deriveFont(24f));
        lblTotal.setForeground(Theme.INK);

        JPanel body = new JPanel(new java.awt.BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(table, java.awt.BorderLayout.CENTER);
        body.add(lblTotal, java.awt.BorderLayout.SOUTH);
        setBody(body);

        reload();
    }

    /** Đọc lại giỏ; tên sản phẩm phải tra DB nên làm ở luồng nền, định dạng tiền làm trên EDT. */
    private void reload() {
        List<OrderDetail> snapshot = new ArrayList<>(CartService.getItems());
        items = snapshot;
        onCartChanged.run();

        Async.run(this, () -> {
            List<String> names = new ArrayList<>();
            for (OrderDetail item : snapshot) {
                names.add(VariantNames.of(item.getProductId()));
            }
            return names;
        }, names -> {
            if (snapshot != items) {
                return; // đã có lần tải mới hơn
            }
            List<Object[]> rows = new ArrayList<>();
            for (int i = 0; i < snapshot.size(); i++) {
                OrderDetail d = snapshot.get(i);
                rows.add(new Object[] {
                        names.get(i),
                        PriceFormat.amount(d.getUnitPrice()),
                        d.getQuantity(),
                        PriceFormat.amount(d.getUnitPrice() * d.getQuantity()) });
            }
            table.setRows(rows);
            lblTotal.setText("Tạm tính: " + PriceFormat.amount(CartService.getSubtotal())
                    + "   (chưa gồm phí vận chuyển)");
        });
    }

    private void onChangeQuantity() {
        int index = table.requireSelection();
        if (index < 0) {
            return;
        }
        OrderDetail item = items.get(index);
        String[] v = new FormDialog(this, "Đổi số lượng")
                .text("Số lượng (0 = xóa khỏi giỏ)", String.valueOf(item.getQuantity()))
                .showDialog();
        if (v == null) {
            return;
        }
        Integer qty = Dialogs.integer(this, v[0], "Số lượng");
        if (qty == null) {
            return;
        }
        String productId = item.getProductId();
        Async.run(this, () -> CartService.updateQuantity(productId, qty), ok -> {
            if (ok) {
                reload();
            } else {
                Dialogs.error(this, "Không đủ tồn kho hoặc sản phẩm đã ngừng bán.");
            }
        });
    }

    private void onRemove() {
        int index = table.requireSelection();
        if (index < 0) {
            return;
        }
        CartService.remove(items.get(index).getProductId());
        reload();
    }

    private void onClear() {
        if (items.isEmpty() || !Dialogs.confirm(this, "Xóa toàn bộ giỏ hàng?")) {
            return;
        }
        CartService.clear();
        reload();
    }

    /** Chọn địa chỉ giao hàng (mặc định chọn sẵn địa chỉ mặc định) rồi đặt đơn. */
    private void onCheckout() {
        if (items.isEmpty()) {
            Dialogs.info(this, "Giỏ hàng đang trống.");
            return;
        }
        String email = account.getEmail();
        Async.run(this, () -> AddressService.getByEmail(email), addresses -> {
            if (addresses.isEmpty()) {
                Dialogs.info(this, "Bạn chưa có địa chỉ giao hàng, hãy thêm địa chỉ trước.");
                onNeedAddress.run();
                return;
            }
            String[] options = new String[addresses.size()];
            int selected = 0;
            for (int i = 0; i < options.length; i++) {
                options[i] = AddressPage.describe(addresses.get(i));
                if (addresses.get(i).isDefault()) {
                    selected = i;
                }
            }
            String[] v = new FormDialog(this, "Xác nhận đặt hàng")
                    .combo("Giao đến", options, selected)
                    .showDialog();
            if (v == null) {
                return;
            }
            Address address = addresses.get(Integer.parseInt(v[0])); // combo trả về chỉ số
            List<OrderDetail> cart = items;
            Async.run(this, () -> OrderService.placeOrder(account.getPhoneNum(), address, cart), orderId -> {
                CartService.clear();
                onCartChanged.run();
                Dialogs.info(this, "Đặt hàng thành công!\nMã đơn: " + orderId);
                onOrdered.run();
            });
        });
    }
}
