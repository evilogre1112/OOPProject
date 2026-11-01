package com.oop.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.oop.Model.OrderDetail;
import com.oop.Model.Product;

public class CartService {

    private static List<OrderDetail> items = new ArrayList<>();

    /** Lấy các dòng trong giỏ (bản chỉ đọc, muốn sửa phải dùng add/update/remove). */
    public static List<OrderDetail> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Thêm biến thể vào giỏ (cộng dồn nếu đã có); chặn nếu ngừng bán hoặc vượt tồn kho. unitPrice lấy từ product.getPrice(). */
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
            existing.setQuantity(newQuantity); // cộng dồn
        } else {
            // orderId = null vì chưa đặt hàng; OrderService.placeOrder sẽ gán khi tạo đơn
            items.add(new OrderDetail(
                    UUID.randomUUID().toString(),
                    quantity,
                    product.getPrice(),
                    product.getId(),
                    null));
        }
        return true;
    }

    /** Đổi số lượng; số lượng <= 0 thì xóa khỏi giỏ. */
    public static boolean updateQuantity(String productId, int quantity) {
        OrderDetail item = findItem(productId);
        if (item == null)
            return false; // sản phẩm không có trong giỏ

        if (quantity <= 0) {
            items.remove(item);
            return true;
        }
        item.setQuantity(quantity);
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