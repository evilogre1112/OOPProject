package com.oop.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.oop.Model.Order;
import com.oop.Model.OrderDetail;

public class OrderDAO {

    /** Thêm đơn hàng trong transaction (không insert Final_amount vì là cột tính tự động). id UUID do service sinh sẵn. */
    public static boolean insertOrder(Connection conn, Order order) throws SQLException {
        return false;
    }

    /** Thêm một dòng chi tiết đơn vào bảng Other_detail trong transaction. */
    public static boolean insertDetail(Connection conn, OrderDetail detail) throws SQLException {
        return false;
    }

    /** Lấy một đơn theo Id. Không có thì trả null. */
    public static Order findById(String orderId) throws SQLException {
        return null;
    }

    /** Lấy đơn của một khách (theo SĐT), mới nhất trước. */
    public static List<Order> findByCustomer(String customerPhone) throws SQLException {
        return null;
    }

    /** Lấy các dòng chi tiết của một đơn. */
    public static List<OrderDetail> findDetailsByOrder(String orderId) throws SQLException {
        return null;
    }

    /** Lấy các dòng chi tiết của một đơn trong transaction đang mở (dùng khi hủy đơn để hoàn kho). */
    public static List<OrderDetail> findDetailsByOrder(Connection conn, String orderId) throws SQLException {
        return null;
    }

    /** Lọc đơn theo Order_status, Payment_status, khoảng ngày Created_at. Tham số null = bỏ qua. */
    public static List<Order> filter(String orderStatus, String paymentStatus,
            LocalDate from, LocalDate to) throws SQLException {
        return null;
    }

    /** Cập nhật Order_status trong transaction (dùng cho hủy đơn + hoàn kho). */
    public static boolean updateOrderStatus(Connection conn, String orderId, String newStatus) throws SQLException {
        return false;
    }

    /** Cập nhật Payment_status. */
    public static boolean updatePaymentStatus(String orderId, String paymentStatus) throws SQLException {
        return false;
    }

    /** Lấy Order_status hiện tại của đơn (để kiểm tra điều kiện hủy / chuyển trạng thái). Không có đơn thì trả null. */
    public static String findOrderStatus(String orderId) throws SQLException {
        return null;
    }
}
