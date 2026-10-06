package com.oop.Service;

import java.util.ArrayList;
import java.util.List;

import com.oop.Model.OrderDetail;
import com.oop.Model.Product;

public class CartService {

    private static List<OrderDetail> items = new ArrayList<>();

    /** Lấy các dòng trong giỏ. */
    public static List<OrderDetail> getItems() {
        return null;
    }

    /** Thêm biến thể vào giỏ (cộng dồn nếu đã có); chặn nếu ngừng bán hoặc vượt tồn kho. unitPrice lấy từ product.getPrice(). */
    public static boolean add(Product product, int quantity) {
        return false;
    }

    /** Đổi số lượng; số lượng <= 0 thì xóa khỏi giỏ. */
    public static boolean updateQuantity(String productId, int quantity) {
        return false;
    }

    /** Xóa một sản phẩm khỏi giỏ. */
    public static void remove(String productId) {
    }

    /** Xóa toàn bộ giỏ. */
    public static void clear() {
    }

    /** Tính tổng tiền giỏ (unitPrice × quantity). */
    public static double getSubtotal() {
        return 0;
    }
}
