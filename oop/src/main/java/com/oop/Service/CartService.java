package com.oop.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.oop.Model.OrderDetail;
import com.oop.Model.Product;

/**
 * Giỏ hàng trong bộ nhớ (README: In-memory List) nên KHÔNG có CartDAO.
 * Giỏ là static, phải gọi clear() khi đăng xuất và sau khi đặt hàng thành công.
 */
public class CartService {

    private static final List<OrderDetail> items = new ArrayList<>();

    /** Lấy các dòng trong giỏ (bản chỉ đọc, muốn sửa phải dùng add/update/remove). */
    public static List<OrderDetail> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Thêm biến thể vào giỏ (cộng dồn nếu đã có); chặn nếu ngừng bán hoặc vượt tồn kho. */
    public static boolean add(Product product, int quantity) {
        if (product == null || quantity <= 0)
            return false;
        if (!product.isActive())
            return false; // sản phẩm ngừng kinh doanh

        OrderDetail existing = findItem(product.getId());
        int newQuantity = quantity + (existing == null ? 0 : existing.getQuantity());

        if (newQuantity > product.getStockQuantity())
            return false; // vượt tồn kho (tính cả số lượng đã có trong giỏ)

        if (existing != null) {
            existing.setQuantity(newQuantity);
            existing.setUnitPrice(product.getPrice()); // đồng bộ giá mới nhất để tổng tiền hiển thị đúng
        } else {
            // orderId = null vì chưa đặt hàng; OrderService.placeOrder sẽ tạo chi tiết mới khi đặt
            items.add(new OrderDetail(
                    UUID.randomUUID().toString(),
                    quantity,
                    product.getPrice(),
                    product.getId(),
                    null));
        }
        return true;
    }

    /**
     * Đổi số lượng; số lượng <= 0 thì xóa khỏi giỏ.
     * Số lượng dương phải còn đủ tồn kho và sản phẩm còn bán (tra lại DB).
     */
    public static boolean updateQuantity(String productId, int quantity) throws SQLException {
        OrderDetail item = findItem(productId);
        if (item == null)
            return false; // sản phẩm không có trong giỏ

        if (quantity <= 0) {
            items.remove(item);
            return true;
        }

        Product product = ProductService.getVariantById(productId);
        if (product == null || !product.isActive() || quantity > product.getStockQuantity())
            return false;

        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        return true;
    }

    /** Xóa một sản phẩm khỏi giỏ. */
    public static void remove(String productId) {
        items.removeIf(item -> item.getProductId() != null && item.getProductId().equals(productId));
    }

    /** Xóa toàn bộ giỏ. */
    public static void clear() {
        items.clear();
    }

    /** Tính tổng tiền giỏ (unitPrice × quantity). */
    public static double getSubtotal() {
        double total = 0;
        for (OrderDetail item : items) {
            total += item.getUnitPrice() * item.getQuantity();
        }
        return total;
    }

    // ===== Helper =====

    /** Tìm dòng trong giỏ theo productId; không có thì trả null. */
    private static OrderDetail findItem(String productId) {
        if (productId == null)
            return null;
        for (OrderDetail item : items) {
            if (productId.equals(item.getProductId()))
                return item;
        }
        return null;
    }
}