package com.oop.DAO;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Review;

public class ReviewDAO {

    /** Thêm đánh giá (khóa: Customer_Phone_num + Products_Id). */
    public static boolean insert(Review review) throws SQLException {
        return false;
    }

    /** Sửa rating/comment của đánh giá theo khóa. */
    public static boolean update(Review review) throws SQLException {
        return false;
    }

    /** Kiểm tra khách đã đánh giá sản phẩm này chưa. */
    public static boolean exists(String customerPhone, String productId) throws SQLException {
        return false;
    }

    /** Kiểm tra khách đã mua biến thể này chưa: có đơn "Hoàn thành" chứa Products_Id trong Other_detail. */
    public static boolean hasPurchased(String customerPhone, String productId) throws SQLException {
        return false;
    }

    /** Lấy các đánh giá của một biến thể, mới nhất trước. */
    public static List<Review> findByProduct(String productId) throws SQLException {
        return null;
    }

    /** Lấy các đánh giá của cả nhóm sản phẩm (JOIN Products), mới nhất trước. */
    public static List<Review> findByGroup(String groupId) throws SQLException {
        return null;
    }

    /** Điểm trung bình của một biến thể (0 nếu chưa có đánh giá). */
    public static double getAverageRating(String productId) throws SQLException {
        return 0;
    }

    /** Điểm trung bình của cả nhóm sản phẩm (0 nếu chưa có đánh giá). */
    public static double getAverageRatingByGroup(String groupId) throws SQLException {
        return 0;
    }
}
