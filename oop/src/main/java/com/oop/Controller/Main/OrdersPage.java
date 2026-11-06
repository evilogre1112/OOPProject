package com.oop.Controller.Main;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.PriceFormat;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Controller.Common.TableDialog;
import com.oop.Model.Account;
import com.oop.Model.Order;
import com.oop.Model.OrderDetail;
import com.oop.Service.OrderService;

/** Lịch sử đơn hàng của khách: xem chi tiết (bấm đúp cũng được) và hủy đơn đang chờ xác nhận. */
public class OrdersPage extends SectionPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Account account;
    private final DataTable table = new DataTable("Mã đơn", "Ngày đặt", "Trạng thái", "Thanh toán",
            "Tạm tính", "Phí vận chuyển", "Tổng cộng");
    private List<Order> orders = new ArrayList<>();

    public OrdersPage(Account account) {
        super("Đơn hàng của tôi");
        this.account = account;

        bar().button("Chi tiết", this::onDetails);
        bar().button("Hủy đơn", this::onCancel);
        bar().button("Làm mới", this::reload);
        table.onDoubleClick(this::onDetails);

        setBody(table);
        reload();
    }

    private void reload() {
        Async.run(this, () -> OrderService.getByCustomer(account.getPhoneNum()), list -> {
            orders = list;
            List<Object[]> rows = new ArrayList<>();
            for (Order o : list) {
                rows.add(new Object[] {
                        shortId(o),
                        o.getCreatedAt() == null ? "" : o.getCreatedAt().format(TIME),
                        orderStatus(o.getOrderStatus()),
                        paymentStatus(o.getPaymentStatus()),
                        PriceFormat.amount(o.getSubtotal()),
                        PriceFormat.amount(o.getShippingFee()),
                        PriceFormat.amount(o.getFinalAmount()) });
            }
            table.setRows(rows);
        });
    }

    private void onDetails() {
        int index = table.requireSelection();
        if (index < 0) {
            return;
        }
        Order order = orders.get(index);

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
            TableDialog.show(this, "Chi tiết đơn " + shortId(order),
                    new String[] { "Sản phẩm", "Đơn giá", "Số lượng", "Thành tiền" }, rows);
        });
    }

    private void onCancel() {
        int index = table.requireSelection();
        if (index < 0) {
            return;
        }
        Order order = orders.get(index);
        if (!Dialogs.confirm(this, "Hủy đơn " + shortId(order) + "?")) {
            return;
        }
        Async.run(this, () -> OrderService.cancel(order.getId()), ok -> {
            if (ok) {
                Dialogs.info(this, "Đã hủy đơn, tồn kho đã được hoàn lại.");
                reload();
            } else {
                Dialogs.error(this, "Chỉ hủy được đơn đang chờ xác nhận và chưa thanh toán.");
            }
        });
    }

    private static String shortId(Order o) {
        String id = o.getId();
        return id.substring(0, Math.min(8, id.length())).toUpperCase();
    }

    private static String orderStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "PENDING" -> "Chờ xác nhận";
            case "SHIPPING" -> "Đang giao";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private static String paymentStatus(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "UNPAID" -> "Chưa thanh toán";
            case "PAID" -> "Đã thanh toán";
            default -> status;
        };
    }
}
