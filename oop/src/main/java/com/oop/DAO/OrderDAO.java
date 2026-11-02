package com.oop.DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.oop.Model.Order;
import com.oop.Model.OrderDetail;

public class OrderDAO {

    /** Thêm đơn hàng trong transaction (không insert Final_amount vì là cột tính tự động). id UUID do service sinh sẵn. 
     * @param conn Connection do Service tạo và quản lý.
     *             Không đóng conn trong DAO.
     * @param order Đơn hàng đã có ID (UUID), trạng thái,
     *              ngày tạo, tổng tiền và SĐT khách hàng.
     * @return true nếu thêm thành công.
    */
    public static boolean insertOrder(Connection conn, Order order) throws SQLException {
        String sql = """
                INSERT INTO Orders (
                    Id,
                    Created_at,
                    Order_status,
                    Payment_status,
                    Subtotal,
                    Shipping_fee,
                    Customer_Phone_num
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getId());
            ps.setTimestamp(2,
                    Timestamp.valueOf(order.getCreatedAt()));
            ps.setString(3, order.getOrderStatus());
            ps.setString(4, order.getPaymentStatus());
            ps.setDouble(5, order.getSubtotal());
            ps.setDouble(6, order.getShippingFee());
            ps.setString(7, order.getCustomerPhoneNum());

            return ps.executeUpdate() == 1;
        }
    }

    /** Thêm một dòng chi tiết đơn vào bảng Other_detail trong transaction.
     * * @param conn Connection của transaction đang mở.
     * @param detail Chi tiết có ID, số lượng, đơn giá,
     *               ID sản phẩm và ID đơn hàng.
     *
     * @return true nếu thêm thành công.
     */
    public static boolean insertDetail(Connection conn, OrderDetail detail) throws SQLException {
        String sql = """
                INSERT INTO Other_detail (
                    Id,
                    Quantity,
                    Unit_price,
                    Products_Id,
                    Orders_Id
                )
                VALUES (?, ?, ?, ?, ?)
                """;
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1,detail.getId());
            ps.setInt(2,detail.getQuantity());
            ps.setDouble(3,detail.getUnitPrice());
            ps.setString(4,detail.getProductId());
            ps.setString(5,detail.getOrderId());
            return ps.executeUpdate() == 1;
        }
    }

    /** Lấy một đơn theo Id. Không có thì trả null. 
     * @param orderId Id của đơn cần tìm.
     * @return Order nếu tìm thấy, null nếu không có.
    */
    public static Order findById(String orderId) throws SQLException {
        String sql ="""
                SELECT * 
                FROM Orders 
                WHERE Id=?
                """;
        try (Connection conn = DBContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, orderId );
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapOrder(rs);
                }
            }
        }
        return null;
    }

    /** Lấy đơn của một khách (theo SĐT), mới nhất trước. */
    public static List<Order> findByCustomer(String customerPhone) throws SQLException {
        List<Order> result  = new ArrayList<>();
        String sql ="""
                SELECT *
                FROM Orders
                WHERE Customer_phone_num=?
                ORDER BY Created_at DESC, Id DESC
            """;
        try (Connection conn=DBContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerPhone);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapOrder(rs));
                }
            }

        }
        return result;
    }

    /** Lấy các dòng chi tiết của một đơn. 
     * @param orderId ID đơn hàng.
     * @return List<OrderDetail>.
     *
     * Tự mở và đóng Connection.
    */
    public static List<OrderDetail> findDetailsByOrder(String orderId) throws SQLException {
        List<OrderDetail> result = new ArrayList<>();
        String sql = """
                SELECT *
                FROM Other_detail
                WHERE Orders_Id=?
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapOrderDetail(rs));
                }
            }
        }
        return result;
    }

    /** Lấy các dòng chi tiết của một đơn trong transaction đang mở (dùng khi hủy đơn để hoàn kho). 
     * @param orderId ID đơn hàng.
     * @return List<OrderDetail>.
    */
    public static List<OrderDetail> findDetailsByOrder(Connection conn, String orderId) throws SQLException {
        List<OrderDetail> result = new ArrayList<>();
        String sql = """
                SELECT *
                FROM Other_detail
                WHERE Orders_Id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapOrderDetail(rs));
                }
            }
        }
        return result;
    }

    /** Lọc đơn theo Order_status, Payment_status, khoảng ngày Created_at. Tham số null = bỏ qua. */
    public static List<Order> filter(String orderStatus, String paymentStatus,
            LocalDate from, LocalDate to) throws SQLException {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được sau ngày kết thúc");
        }

        List<Order> result = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM Orders
                WHERE 1 = 1
                """);

        if (orderStatus != null && !orderStatus.isBlank()) {
            sql.append(" AND Order_status = ?");
        }

        if (paymentStatus != null && !paymentStatus.isBlank()) {
            sql.append(" AND Payment_status = ?");
        }

        if (from != null) {
            sql.append(" AND Created_at >= ?");
        }

        if (to != null) {
            sql.append(" AND Created_at < ?");
        }

        sql.append(" ORDER BY Created_at DESC, Id DESC");

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (orderStatus != null && !orderStatus.isBlank()) {
                ps.setString(index++, orderStatus);
            }

            if (paymentStatus != null && !paymentStatus.isBlank()) {
                ps.setString(index++, paymentStatus);
            }

            if (from != null) {
                ps.setTimestamp(index++,
                        Timestamp.valueOf(from.atStartOfDay()));
            }

            if (to != null) {
                ps.setTimestamp(index++,
                        Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapOrder(rs));
                }
            }
        }

        return result;
    }

    /** Cập nhật Order_status trong transaction (dùng cho hủy đơn + hoàn kho).
     * @param conn Connection của transaction đang mở.
     * @param orderId ID đơn hàng.
     * @param newStatus Trạng thái mới.
     */
    public static boolean updateOrderStatus(Connection conn, String orderId, String newStatus) throws SQLException {
        String sql = """
                UPDATE Orders
                SET Order_status = ?
                WHERE Id=?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Cập nhật Payment_status. */
    public static boolean updatePaymentStatus(String orderId, String paymentStatus) throws SQLException {
        String sql = """
                UPDATE Orders
                SET Payment_status = ?
                WHERE Id=?
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paymentStatus);
            ps.setString(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Lấy Order_status hiện tại của đơn (để kiểm tra điều kiện hủy / chuyển trạng thái). Không có đơn thì trả null. */
    public static String findOrderStatus(String orderId) throws SQLException {
        String sql = """
                SELECT Order_status
                FROM Orders
                WHERE Id=?
                """;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Order_status");
                }
            }
        }
        return null;
    }
     /**
     * Chuyển ResultSet thành Order.
     * Chỉ sử dụng nội bộ DAO.
     */
    private static Order mapOrder(ResultSet rs)
            throws SQLException {

        Timestamp createdAt = rs.getTimestamp("Created_at");

        return new Order(
                rs.getString("Id"),
                createdAt == null
                        ? null
                        : createdAt.toLocalDateTime(),
                rs.getString("Order_status"),
                rs.getString("Payment_status"),
                rs.getDouble("Subtotal"),
                rs.getDouble("Shipping_fee"),
                rs.getString("Customer_Phone_num")
        );
    }

    /**
     * Chuyển ResultSet thành OrderDetail.
     * Chỉ sử dụng nội bộ DAO.
     */
    private static OrderDetail mapOrderDetail(ResultSet rs)
            throws SQLException {

        return new OrderDetail(
                rs.getString("Id"),
                rs.getInt("Quantity"),
                rs.getDouble("Unit_price"),
                rs.getString("Products_Id"),
                rs.getString("Orders_Id")
        );
    }
    public static String findOrderStatusForUpdate(
        Connection conn, String orderId) throws SQLException {

        String sql = """
            SELECT Order_status
            FROM Orders
            WHERE Id = ?
            FOR UPDATE
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? rs.getString("Order_status")
                        : null;
            }
        }
    }
}

