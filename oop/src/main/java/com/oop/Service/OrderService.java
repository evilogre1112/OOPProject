package com.oop.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.oop.DAO.DBContext;
import com.oop.DAO.OrderDAO;
import com.oop.Model.Address;
import com.oop.Model.Order;
import com.oop.Model.OrderDetail;


public class OrderService {
    private static final String PENDING = "PENDING";// Chờ xác nhận
    private static final String SHIPPING = "SHIPPING";// Đang giao
    private static final String COMPLETED = "COMPLETED";// Hoàn thành
    private static final String CANCELLED = "CANCELLED";// Hủy

    private static final String UNPAID = "UNPAID";// Chưa thanh toán
    private static final String PAID = "PAID";// Đã thanh toán

    // ===== Customer =====

    /** Đặt hàng bằng transaction: kiểm tra tồn kho + isActive, tạo Order + OrderDetail, trừ tồn kho; lỗi thì rollback. Xong thì xóa giỏ. Trả về id đơn. 
     * @param customerPhone SĐT của khách hàng đang đặt hàng.
     *                      Phải tồn tại trong bảng Customer.
     *
     * @param address Địa chỉ giao hàng.
     *                Hiện được dùng để xác nhận có địa chỉ;
     *                schema Orders chưa có cột lưu địa chỉ.
     *
     * @param cartItems Danh sách sản phẩm trong giỏ.
     *                  Mỗi OrderDetail phải có:
     *                  - productId: ID sản phẩm
     *                  - quantity: số lượng mua (> 0)
     *
     *                  Không cần truyền ID chi tiết, ID đơn
     *                  hoặc đơn giá. Service tự xác định.
     *
     * @return UUID của đơn hàng mới tạo.
     *
     * @throws SQLException Nếu khách hàng không tồn tại,
     *                      sản phẩm hết hàng, ngừng bán,
     *                      hoặc transaction thất bại.
     *

    */
    public static String placeOrder(String customerPhone, Address address, List<OrderDetail> cartItems) throws SQLException {
        if (customerPhone == null || customerPhone.isBlank()) {
            throw new IllegalArgumentException(
                    "Số điện thoại khách hàng không hợp lệ");
        }

        if (address == null) {
            throw new IllegalArgumentException(
                    "Chưa chọn địa chỉ giao hàng");
        }

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Giỏ hàng đang trống");
        }
        Set<String> productIds = new HashSet<>();
        //kiểm tra xem có sản phẩm ko tồn tại ko
        for(OrderDetail item : cartItems) {
            if (item.getProductId() == null || item.getProductId().isBlank()) {
                throw new IllegalArgumentException(
                        "Mã sản phẩm không hợp lệ");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Số lượng sản phẩm phải lớn hơn 0");
            }
            if (!productIds.add(item.getProductId())) {
                throw new IllegalArgumentException(
                        "Có sản phẩm bị trùng trong giỏ hàng: " + item.getProductId());
            }
        }try (Connection conn = DBContext.getConnection()) {

            conn.setAutoCommit(false);

            try {
                // Bước 1: Khóa và kiểm tra tồn kho.
                if (!checkStock(conn, cartItems)) {
                    throw new SQLException(
                            "Có sản phẩm hết hàng hoặc ngừng bán");
                }

                // Bước 2: Lấy giá từ databases
                String priceSql = """
                        SELECT Price
                        FROM Products
                        WHERE Id = ?
                        """;

                List<OrderDetail> details = new ArrayList<>();
                String orderId = UUID.randomUUID().toString();
                double subtotal = 0;

                try (PreparedStatement ps =
                        conn.prepareStatement(priceSql)) {

                    for (OrderDetail item : cartItems) {
                        ps.setString(1, item.getProductId());

                        double price;

                        try(ResultSet res = ps.executeQuery()) {
                            if(res.next()==false) {
                                throw new SQLException(
                                        "Sản phẩm không tồn tại: "
                                        + item.getProductId());
                            }

                            price = res.getDouble("Price");
                        }

                        if (!Double.isFinite(price) || price < 0) {
                            throw new SQLException(
                                    "Giá sản phẩm không hợp lệ: "
                                    + item.getProductId());
                        }

                        double lineTotal =
                                price * item.getQuantity();

                        if (!Double.isFinite(lineTotal)) {
                            throw new SQLException(
                                    "Tổng tiền vượt giới hạn");
                        }

                        subtotal += lineTotal;

                        OrderDetail detail = new OrderDetail(
                                UUID.randomUUID().toString(),
                                item.getQuantity(),
                                price,
                                item.getProductId(),
                                orderId
                        );

                        details.add(detail);
                    }
                }

                if (!Double.isFinite(subtotal)) {
                    throw new SQLException(
                            "Tổng tiền đơn hàng không hợp lệ");
                }

                // Bước 3: Tạo đối tượng Order.
                double shippingFee = calcShippingFee(address);

                Order order = new Order(
                        orderId,
                        LocalDateTime.now(),
                        PENDING,
                        UNPAID,
                        subtotal,
                        shippingFee,
                        customerPhone
                );

                // Bước 4: Thêm đơn.
                if (!OrderDAO.insertOrder(conn, order)) {
                    throw new SQLException("Không thể tạo đơn hàng");
                }

                // Bước 5: Thêm chi tiết và trừ tồn kho.
                String stockSql = """
                        UPDATE Products
                        SET Stock_quantity = Stock_quantity - ?
                        WHERE Id = ?
                          AND Is_active = 1
                          AND Stock_quantity >= ?
                        """;

                try (PreparedStatement stockPs =
                        conn.prepareStatement(stockSql)) {

                    for (OrderDetail detail : details) {

                        if (!OrderDAO.insertDetail(conn, detail)) {
                            throw new SQLException(
                                    "Không thể thêm chi tiết đơn hàng");
                        }

                        stockPs.setInt(1, detail.getQuantity());
                        stockPs.setString(2, detail.getProductId());
                        stockPs.setInt(3, detail.getQuantity());

                        if (stockPs.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Không đủ tồn kho cho sản phẩm: "
                                    + detail.getProductId());
                        }
                    }
                }

                // Bước 6: Xác nhận transaction.
                conn.commit();

                return orderId;

            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    /** Lấy lịch sử mua hàng của khách (theo SĐT), mới nhất trước. 
     * @param customerPhone SĐT khách hàng.
     * @return Danh sách Order, mới nhất trước.
     *
    */
    public static List<Order> getByCustomer(String customerPhone) throws SQLException {
         if (customerPhone == null || customerPhone.isBlank()) {
            throw new IllegalArgumentException(
                    "Số điện thoại không hợp lệ");
        }
        return OrderDAO.findByCustomer(customerPhone);
    }

    /** Lấy chi tiết (OrderDetail) của một đơn. */
    public static List<OrderDetail> getDetails(String orderId) throws SQLException {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không hợp lệ");
        }
        return OrderDAO.findDetailsByOrder(orderId);
    }

    /** Hủy đơn: chỉ khi đang "Chờ xác nhận"; hoàn lại tồn kho trong cùng transaction. 
     * @param orderId ID đơn cần hủy.
     * @return true nếu hủy thành công;
     *         false nếu trạng thái không cho phép hủy.
     *
     * Chỉ cho phép:
     * - Order_status = PENDING
     * - Payment_status = UNPAID
     * Các bước:
     * 1. Bắt đầu transaction.
     * 2. Khóa đơn bằng SELECT ... FOR UPDATE.
     * 3. Kiểm tra trạng thái sau khi khóa.
     * 4. Hoàn tồn kho.
     * 5. Đổi trạng thái CANCELLED.
     * 6. COMMIT hoặc ROLLBACK.
     *
    */
    public static boolean cancel(String orderId) throws SQLException {
         requireOrderId(orderId);

        try (Connection conn = DBContext.getConnection()) {

            conn.setAutoCommit(false);

            try {
                String status =
                        OrderDAO.findOrderStatusForUpdate(
                                conn, orderId);

                if (status == null) {
                    throw new SQLException(
                            "Đơn hàng không tồn tại");
                }

                if (!PENDING.equals(status)) {
                    conn.rollback();
                    return false;
                }

                String payment = findPaymentStatus(conn, orderId);

                if (!UNPAID.equals(payment)) {
                    conn.rollback();
                    return false;
                }

                restoreStock(conn, orderId);

                if (!OrderDAO.updateOrderStatus(
                        conn, orderId, CANCELLED)) {
                    throw new SQLException(
                            "Không thể cập nhật trạng thái");
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    // ===== Admin =====

    /** [Admin] Lọc đơn theo trạng thái đơn, trạng thái thanh toán, khoảng thời gian. Tham số null = bỏ qua. */
    public static List<Order> filter(String orderStatus, String paymentStatus,
            LocalDate from, LocalDate to) throws SQLException {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }

        return OrderDAO.filter(
                orderStatus, paymentStatus, from, to);
    }

    /** [Admin] Cập nhật tiến độ: Chờ xác nhận → Đang giao → Hoàn thành / Hủy (sai thứ tự thì từ chối). Hủy thì hoàn kho. */
    public static boolean updateOrderStatus(String orderId, String newStatus) throws SQLException {
        requireOrderId(orderId);

        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Trạng thái mới không hợp lệ");
        }

        if (CANCELLED.equals(newStatus)) {
            return cancel(orderId);
        }

        try (Connection conn = DBContext.getConnection()) {

            conn.setAutoCommit(false);

            try {
                String current =
                        OrderDAO.findOrderStatusForUpdate(
                                conn, orderId);

                if (current == null) {
                    throw new SQLException(
                            "Đơn hàng không tồn tại");
                }

                boolean valid =
                        (PENDING.equals(current)
                                && SHIPPING.equals(newStatus))
                        ||
                        (SHIPPING.equals(current)
                                && COMPLETED.equals(newStatus));

                if (!valid) {
                    conn.rollback();
                    return false;
                }

                boolean updated =
                        OrderDAO.updateOrderStatus(
                                conn, orderId, newStatus);

                if (!updated) {
                    throw new SQLException(
                            "Không thể cập nhật đơn hàng");
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }    

    /** [Admin] Cập nhật trạng thái thanh toán. */
    public static boolean updatePaymentStatus(String orderId, String paymentStatus) throws SQLException {
        requireOrderId(orderId);

        if (!PAID.equals(paymentStatus)) {
            return false;
        }

        try (Connection conn = DBContext.getConnection()) {

            conn.setAutoCommit(false);

            try {
                String orderStatus =
                        OrderDAO.findOrderStatusForUpdate(
                                conn, orderId);

                if (orderStatus == null) {
                    throw new SQLException(
                            "Đơn hàng không tồn tại");
                }

                if (CANCELLED.equals(orderStatus)) {
                    conn.rollback();
                    return false;
                }

                String currentPayment =
                        findPaymentStatus(conn, orderId);

                if (!UNPAID.equals(currentPayment)) {
                    conn.rollback();
                    return false;
                }

                // Cập nhật trên CÙNG transaction.
                // Không dùng phiên bản DAO tự mở Connection.
                String sql = """
                        UPDATE Orders
                        SET Payment_status = ?
                        WHERE Id = ?
                          AND Payment_status = ?
                        """;

                try (PreparedStatement ps =
                        conn.prepareStatement(sql)) {

                    ps.setString(1, PAID);
                    ps.setString(2, orderId);
                    ps.setString(3, UNPAID);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Cập nhật thanh toán thất bại");
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    /** [Admin] Xuất hóa đơn ra file text. */
    public static void exportInvoice(String orderId, String filePath) throws SQLException {
        requireOrderId(orderId);

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "Đường dẫn file không hợp lệ");
        }

        Order order = OrderDAO.findById(orderId);

        if (order == null) {
            throw new SQLException("Đơn hàng không tồn tại");
        }

        List<OrderDetail> details =
                OrderDAO.findDetailsByOrder(orderId);

        StringBuilder text = new StringBuilder();

        text.append("========== HOA DON DON HANG ==========\n");
        text.append("Ma don: ").append(order.getId()).append("\n");
        text.append("Ngay tao: ").append(order.getCreatedAt()).append("\n");
        text.append("Khach hang: ")
                .append(order.getCustomerPhoneNum()).append("\n");
        text.append("Trang thai: ")
                .append(order.getOrderStatus()).append("\n");
        text.append("Thanh toan: ")
                .append(order.getPaymentStatus()).append("\n");

        text.append("--------------------------------------\n");

        for (OrderDetail detail : details) {
            text.append("San pham: ")
                    .append(detail.getProductId()).append("\n");
            text.append("So luong: ")
                    .append(detail.getQuantity()).append("\n");
            text.append("Don gia: ")
                    .append(detail.getUnitPrice()).append("\n");
            text.append("Thanh tien: ")
                    .append(detail.getQuantity()
                            * detail.getUnitPrice())
                    .append("\n");
            text.append("--------------------------------------\n");
        }

        text.append("Tam tinh: ")
                .append(order.getSubtotal()).append("\n");
        text.append("Phi van chuyen: ")
                .append(order.getShippingFee()).append("\n");
        text.append("Tong cong: ")
                .append(order.getFinalAmount()).append("\n");

        try {
            Files.writeString(
                    Path.of(filePath),
                    text.toString(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException | RuntimeException e) {
            throw new SQLException(
                    "Không thể xuất hóa đơn: " + e.getMessage(), e);
        }
    }

    // ===== Helper =====

    /** Tính phí ship theo địa chỉ (vd: cùng thành phố rẻ hơn, hoặc phí cố định). */
    private static double calcShippingFee(Address address) {
        if (address == null) {
            throw new IllegalArgumentException(
                    "Địa chỉ không được null");
        }

        return 30000.0;
    }

    /** Kiểm tra mọi dòng trong giỏ còn đủ tồn kho và đang bán. */
    private static boolean checkStock(Connection conn, List<OrderDetail> items) throws SQLException {
         if (items == null || items.isEmpty()) {
            return false;
        }

        TreeMap<String, Long> required = new TreeMap<>();

        for (OrderDetail item : items) {
            if (item == null
                    || item.getProductId() == null
                    || item.getProductId().isBlank()
                    || item.getQuantity() <= 0) {
                return false;
            }

            required.merge(
                    item.getProductId(),
                    (long) item.getQuantity(),
                    Long::sum);
        }

        String sql = """
                SELECT Stock_quantity, Is_active
                FROM Products
                WHERE Id = ?
                FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            for (var entry : required.entrySet()) {

                ps.setString(1, entry.getKey());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return false;
                    }

                    if (!rs.getBoolean("Is_active")) {
                        return false;
                    }

                    if (rs.getLong("Stock_quantity")
                            < entry.getValue()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /** Cộng lại tồn kho các sản phẩm trong đơn (khi hủy). */
    private static void restoreStock(Connection conn, String orderId) throws SQLException {
        List<OrderDetail> details =
                OrderDAO.findDetailsByOrder(conn, orderId);

        if (details.isEmpty()) {
            throw new SQLException(
                    "Đơn hàng không có chi tiết để hoàn kho");
        }

        String sql = """
                UPDATE Products
                SET Stock_quantity = Stock_quantity + ?
                WHERE Id = ?
                """;

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            for (OrderDetail detail : details) {

                if (detail.getQuantity() <= 0) {
                    throw new SQLException(
                            "Số lượng hoàn kho không hợp lệ");
                }

                ps.setInt(1, detail.getQuantity());
                ps.setString(2, detail.getProductId());

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                            "Hoàn kho thất bại: "
                            + detail.getProductId());
                }
            }
        }
    }
     /**
     * Đọc Payment_status bằng Connection hiện tại.
     *
     * Gọi sau khi đã khóa bản ghi Orders.
     */
    private static String findPaymentStatus(
            Connection conn,
            String orderId) throws SQLException {

        String sql = """
                SELECT Payment_status
                FROM Orders
                WHERE Id = ?
                """;

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getString("Payment_status")
                        : null;
            }
        }
    }

    /**
     * Kiểm tra ID đơn hàng.
     */
    private static void requireOrderId(String orderId) {

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException(
                    "ID đơn hàng không được để trống");
        }
    }

    /**
     * Rollback và giữ lại nguyên nhân lỗi ban đầu.
     */
    private static void rollback(
            Connection conn,
            Exception originalError) {

        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }
}
