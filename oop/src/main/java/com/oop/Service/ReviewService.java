package com.oop.Service;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Review;

public class ReviewService {

    /** Thêm đánh giá: chỉ khi khách đã mua (đơn Hoàn thành) và chưa đánh giá sản phẩm đó (khóa phone + productId). */
    public static boolean add(Review review) throws SQLException {
        return false;
    }

    /** Sửa đánh giá của chính khách đó. */
    public static boolean update(Review review) throws SQLException {
        return false;
    }

    /** Lấy các đánh giá của một sản phẩm. */
    public static List<Review> getByProduct(String productId) throws SQLException {
        return null;
    }

    /** Điểm đánh giá trung bình của sản phẩm (0 nếu chưa có). */
    public static double getAverageRating(String productId) throws SQLException {
        return 0;
    }

    /** Kiểm tra khách đã mua sản phẩm (đơn Hoàn thành chứa productId). */
    private static boolean hasPurchased(String customerPhone, String productId) throws SQLException {
        return false;
    }
}
