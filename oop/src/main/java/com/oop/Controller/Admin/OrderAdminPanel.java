package com.oop.Controller.Admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.oop.Controller.Common.ActionBar;
import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.PriceFormat;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Controller.Common.TableDialog;
import com.oop.Controller.Common.Theme;
import com.oop.Controller.Main.VariantNames;
import com.oop.Model.Order;
import com.oop.Model.OrderDetail;
import com.oop.Service.AccountService;
import com.oop.Service.AdjustService;
import com.oop.Service.OrderService;

/**
 * Trang Đơn hàng: lọc theo trạng thái / thanh toán / khoảng ngày, xem chi tiết,
 * chuyển tiến độ (Chờ xác nhận → Đang giao → Hoàn thành), hủy đơn, xác nhận đã
 * thanh toán, xuất hóa đơn.
 */
public class OrderAdminPanel extends SectionPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Phần tử đầu là "Tất cả" (key null = bỏ qua khi lọc); thứ tự tên khớp thứ tự
    // key
    private static final String[] STATUS_KEYS = { null, "PENDING", "SHIPPING", "COMPLETED", "CANCELLED" };
    private static final String[] STATUS_NAMES = { "Tất cả", "Chờ xác nhận", "Đang giao", "Hoàn thành", "Đã hủy" };
    private static final String[] PAYMENT_KEYS = { null, "UNPAID", "PAID" };
    private static final String[] PAYMENT_NAMES = { "Tất cả", "Chưa thanh toán", "Đã thanh toán" };

    private final AccountService accountService;
    private final DataTable table = new DataTable("Mã đơn", "Ngày đặt", "Khách hàng (SĐT)", "Trạng thái",
            "Thanh toán", "Tạm tính", "Phí vận chuyển", "Tổng cộng");
    private final JComboBox<String> cbStatus = new JComboBox<>(STATUS_NAMES);
    private final JComboBox<String> cbPayment = new JComboBox<>(PAYMENT_NAMES);
    private final JTextField txtFrom;
    private final JTextField txtTo;
    private List<Order> orders = new ArrayList<>();

    public OrderAdminPanel(AccountService accountService) {
        super("Đơn hàng");
        this.accountService = accountService;

        // Hàng lọc
        bar().addField(Theme.fieldLabel("Trạng thái"));
        bar().addField(styled(cbStatus));
        bar().addField(Theme.fieldLabel("Thanh toán"));
        bar().addField(styled(cbPayment));
        txtFrom = bar().textField("Từ ngày", "", 110);
        txtTo = bar().textField("Đến ngày", "", 110);
        txtFrom.setToolTipText("yyyy-MM-dd, ví dụ 2026-11-01");
        txtTo.setToolTipText("yyyy-MM-dd, ví dụ 2026-11-30");
        bar().button("Lọc", this::reload);
        bar().button("Tất cả", this::onClear);

        // Hàng thao tác trên đơn đang chọn
        ActionBar actions = new ActionBar();
        actions.button("Chi tiết", this::onDetails);
        actions.button("Giao hàng", this::onShip);
        actions.button("Hoàn thành", this::onComplete);
        actions.button("Hủy đơn", this::onCancel);
        actions.button("Đã thanh toán", this::onPaid);
        actions.button("Xuất hóa đơn", this::onInvoice);
        table.onDoubleClick(this::onDetails);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(actions, BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        setBody(body);

        reload();
    }

    // ===== Danh sách =====

    private void reload() {
        if (!validDate(txtFrom, "Từ ngày") || !validDate(txtTo, "Đến ngày")) {
            return;
        }
        String orderStatus = STATUS_KEYS[cbStatus.getSelectedIndex()];
        String paymentStatus = PAYMENT_KEYS[cbPayment.getSelectedIndex()];
        LocalDate from = dateOf(txtFrom);
        LocalDate to = dateOf(txtTo);

        Async.run(this, () -> OrderService.filter(orderStatus, paymentStatus, from, to), list -> {
            orders = list;
            List<Object[]> rows = new ArrayList<>();
            for (Order o : list) {
                rows.add(new Object[] {
                        shortId(o),
                        o.getCreatedAt() == null ? "" : o.getCreatedAt().format(TIME),
                        o.getCustomerPhoneNum(),
                        nameOf(STATUS_KEYS, STATUS_NAMES, o.getOrderStatus()),
                        nameOf(PAYMENT_KEYS, PAYMENT_NAMES, o.getPaymentStatus()),
                        PriceFormat.amount(o.getSubtotal()),
                        PriceFormat.amount(o.getShippingFee()),
                        PriceFormat.amount(o.getFinalAmount()) });
            }
            table.setRows(rows);
        });
    }

    private void onClear() {
        cbStatus.setSelectedIndex(0);
        cbPayment.setSelectedIndex(0);
        txtFrom.setText("");
        txtTo.setText("");
        reload();
    }

    // ===== Thao tác =====

    private void onDetails() {
        Order order = selectedOrder();
        if (order == null) {
            return;
        }
        // Luồng nền chỉ lấy số liệu thô; định dạng tiền làm trên EDT
        Async.run(this, () -> {
            List<Object[]> raw = new ArrayList<>();
            for (OrderDetail d : OrderService.getDetails(order.getId())) {
                raw.add(new Object[] { VariantNames.of(d.getProductId()), d.getUnitPrice(), d.getQuantity() });
            }
            return raw;
        }, raw -> {
            List<Object[]> rows = new ArrayList<>();
            for (Object[] r : raw) {
                double price = (Double) r[1];
                int qty = (Integer) r[2];
                rows.add(new Object[] { r[0], PriceFormat.amount(price), qty, PriceFormat.amount(price * qty) });
            }
            TableDialog.show(this, "Đơn " + shortId(order) + " - " + order.getCustomerPhoneNum(),
                    new String[] { "Sản phẩm", "Đơn giá", "Số lượng", "Thành tiền" }, rows);
        });
    }

    private void onShip() {
        Order order = selectedOrder();
        if (order == null) {
            return;
        }
        if (!"PENDING".equals(order.getOrderStatus())) {
            Dialogs.info(this, "Chỉ đơn \"Chờ xác nhận\" mới chuyển sang \"Đang giao\" được.");
            return;
        }
        change(order, "Chuyển đơn " + shortId(order) + " sang \"Đang giao\"?", "→ Đang giao",
                "Không chuyển được, trạng thái đơn đã thay đổi.",
                () -> OrderService.updateOrderStatus(order.getId(), "SHIPPING"));
    }

    private void onComplete() {
        Order order = selectedOrder();
        if (order == null) {
            return;
        }
        if (!"SHIPPING".equals(order.getOrderStatus())) {
            Dialogs.info(this, "Chỉ đơn \"Đang giao\" mới chuyển sang \"Hoàn thành\" được.");
            return;
        }
        change(order, "Đánh dấu đơn " + shortId(order) + " đã hoàn thành?", "→ Hoàn thành",
                "Không chuyển được, trạng thái đơn đã thay đổi.",
                () -> OrderService.updateOrderStatus(order.getId(), "COMPLETED"));
    }

    private void onCancel() {
        Order order = selectedOrder();
        if (order == null) {
            return;
        }
        if (!"PENDING".equals(order.getOrderStatus()) || !"UNPAID".equals(order.getPaymentStatus())) {
            Dialogs.info(this, "Chỉ hủy được đơn \"Chờ xác nhận\" và chưa thanh toán.");
            return;
        }
        change(order, "Hủy đơn " + shortId(order) + "? Tồn kho sẽ được hoàn lại.", "→ Đã hủy",
                "Không hủy được, trạng thái đơn đã thay đổi.",
                () -> OrderService.updateOrderStatus(order.getId(), "CANCELLED"));
    }

    private void onPaid() {
        Order order = selectedOrder();
        if (order == null) {
            return;
        }
        if ("CANCELLED".equals(order.getOrderStatus()) || !"UNPAID".equals(order.getPaymentStatus())) {
            Dialogs.info(this, "Chỉ xác nhận thanh toán cho đơn chưa thanh toán và chưa hủy.");
            return;
        }
        change(order, "Xác nhận đơn " + shortId(order) + " đã thanh toán?", "→ Đã thanh toán",
                "Không cập nhật được, trạng thái đơn đã thay đổi.",
                () -> OrderService.updatePaymentStatus(order.getId(), "PAID"));
    }

    private void onInvoice() {
        Order order = selectedOrder();
        if (order == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn");
        chooser.setSelectedFile(new File("hoadon_" + shortId(order) + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.exists() && !Dialogs.confirm(this, "File đã tồn tại, ghi đè?")) {
            return;
        }
        String path = file.getAbsolutePath();
        Async.run(this, () -> {
            OrderService.exportInvoice(order.getId(), path);
            return path;
        }, saved -> Dialogs.info(this, "Đã xuất hóa đơn:\n" + saved));
    }

    /**
     * Hỏi xác nhận, chạy việc đổi trạng thái ở luồng nền; thành công thì ghi nhật
     * ký rồi tải lại,
     * service từ chối (trả false) thì báo refuse.
     */
    private void change(Order order, String question, String note, String refuse, Callable<Boolean> task) {
        if (!Dialogs.confirm(this, question)) {
            return;
        }
        Async.run(this, () -> {
            boolean ok = task.call();
            if (ok) {
                log(order, note);
            }
            return ok;
        }, ok -> {
            if (ok) {
                reload();
            } else {
                Dialogs.error(this, refuse);
            }
        });
    }

    // ===== Helper =====

    /** Dòng đang chọn; chưa chọn thì đã báo cho người dùng và trả về null. */
    private Order selectedOrder() {
        int index = table.requireSelection();
        return index < 0 ? null : orders.get(index);
    }

    /**
     * Ghi nhật ký an toàn: ghi lỗi thì chỉ in ra, không làm hỏng việc chính đã
     * thành công.
     */
    private void log(Order order, String note) {
        try {
            AdjustService.log(accountService, "UPDATE", "Đơn " + shortId(order) + " " + note, "Đơn hàng",
                    order.getId());
        } catch (Exception e) {
            System.err.println("Ghi nhật ký điều chỉnh không thành công: " + e.getMessage());
        }
    }

    private static String shortId(Order o) {
        String id = o.getId();
        return id.substring(0, Math.min(8, id.length())).toUpperCase();
    }

    /** Tên hiển thị của một key; không có trong danh sách thì trả về chính key. */
    private static String nameOf(String[] keys, String[] names, String key) {
        for (int i = 1; i < keys.length; i++) { // bỏ qua dòng "Tất cả"
            if (keys[i].equals(key)) {
                return names[i];
            }
        }
        return key == null ? "" : key;
    }

    private static JComboBox<String> styled(JComboBox<String> box) {
        box.setFont(Theme.BODY);
        box.setPreferredSize(new Dimension(150, 38));
        return box;
    }

    /** Ô trống là hợp lệ (bỏ qua); có chữ thì phải đúng yyyy-MM-dd. */
    private boolean validDate(JTextField field, String label) {
        return field.getText().trim().isEmpty() || Dialogs.date(this, field.getText(), label) != null;
    }

    private LocalDate dateOf(JTextField field) {
        String text = field.getText().trim();
        return text.isEmpty() ? null : LocalDate.parse(text);
    }
}
