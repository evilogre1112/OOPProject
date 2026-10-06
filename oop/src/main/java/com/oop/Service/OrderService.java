package com.oop.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.oop.Model.Address;
import com.oop.Model.Order;
import com.oop.Model.OrderDetail;

public class OrderService {

    // ===== Customer =====

    /** Đặt hàng bằng transaction: kiểm tra tồn kho + isActive, tạo Order + OrderDetail, trừ tồn kho; lỗi thì rollback. Xong thì xóa giỏ. Trả về id đơn. */
    public static String placeOrder(String customerPhone, Address address, List<OrderDetail> cartItems) throws SQLException {
        return null;
    }

    /** Lấy lịch sử mua hàng của khách (theo SĐT), mới nhất trước. */
    public static List<Order> getByCustomer(String customerPhone) throws SQLException {
        return null;
    }

    /** Lấy chi tiết (OrderDetail) của một đơn. */
    public static List<OrderDetail> getDetails(String orderId) throws SQLException {
        return null;
    }

    /** Hủy đơn: chỉ khi đang "Chờ xác nhận"; hoàn lại tồn kho trong cùng transaction. */
    public static boolean cancel(String orderId) throws SQLException {
        return false;
    }

    // ===== Admin =====

    /** [Admin] Lọc đơn theo trạng thái đơn, trạng thái thanh toán, khoảng thời gian. Tham số null = bỏ qua. */
    public static List<Order> filter(String orderStatus, String paymentStatus,
            LocalDate from, LocalDate to) throws SQLException {
        return null;
    }

    /** [Admin] Cập nhật tiến độ: Chờ xác nhận → Đang giao → Hoàn thành / Hủy (sai thứ tự thì từ chối). Hủy thì hoàn kho. */
    public static boolean updateOrderStatus(String orderId, String newStatus) throws SQLException {
        return false;
    }

    /** [Admin] Cập nhật trạng thái thanh toán. */
    public static boolean updatePaymentStatus(String orderId, String paymentStatus) throws SQLException {
        return false;
    }

    /** [Admin] Xuất hóa đơn ra file text. */
    public static void exportInvoice(String orderId, String filePath) throws SQLException {
    }

    // ===== Helper =====

    /** Tính phí ship theo địa chỉ (vd: cùng thành phố rẻ hơn, hoặc phí cố định). */
    private static double calcShippingFee(Address address) {
        return 0;
    }

    /** Kiểm tra mọi dòng trong giỏ còn đủ tồn kho và đang bán. */
    private static boolean checkStock(Connection conn, List<OrderDetail> items) throws SQLException {
        return false;
    }

    /** Cộng lại tồn kho các sản phẩm trong đơn (khi hủy). */
    private static void restoreStock(Connection conn, String orderId) throws SQLException {
    }
}
