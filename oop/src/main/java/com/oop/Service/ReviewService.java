package com.oop.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.oop.DAO.ReviewDAO;
import com.oop.Model.Review;

public class ReviewService {

    /**
     * Thêm đánh giá: chỉ khi khách đã mua (đơn Hoàn thành) và chưa đánh giá sản phẩm đó (khóa phone + productId).
     */
    public static boolean add(Review review) throws SQLException {
        if (review == null
                || isNullOrEmpty(review.getCustomerPhoneNum())
                || isNullOrEmpty(review.getProductId())) {
            return false;
        }

        // Điểm đánh giá phải từ 1 đến 5 sao
        if (review.getRating() < 1 || review.getRating() > 5) {
            return false;
        }

        String phone = review.getCustomerPhoneNum().trim();
        String productId = review.getProductId().trim();
        review.setCustomerPhoneNum(phone);
        review.setProductId(productId);

        if (review.getComment() != null) {
            review.setComment(review.getComment().trim());
        }

        // 1. Kiểm tra khách hàng đã mua sản phẩm đó với đơn hàng hoàn thành chưa
        if (!hasPurchased(phone, productId)) {
            return false;
        }

        // 2. Kiểm tra khách hàng đã đánh giá sản phẩm này trước đó chưa
        if (ReviewDAO.exists(phone, productId)) {
            return false;
        }

        // Nếu chưa set thời gian tạo thì gán thời gian hiện tại
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }

        return ReviewDAO.insert(review);
    }

    /**
     * Sửa đánh giá của chính khách đó.
     */
    public static boolean update(Review review) throws SQLException {
        if (review == null
                || isNullOrEmpty(review.getCustomerPhoneNum())
                || isNullOrEmpty(review.getProductId())) {
            return false;
        }

        // Điểm đánh giá phải từ 1 đến 5 sao
        if (review.getRating() < 1 || review.getRating() > 5) {
            return false;
        }

        String phone = review.getCustomerPhoneNum().trim();
        String productId = review.getProductId().trim();
        review.setCustomerPhoneNum(phone);
        review.setProductId(productId);

        if (review.getComment() != null) {
            review.setComment(review.getComment().trim());
        }

        // Phải có đánh giá tồn tại từ trước thì mới cập nhật
        if (!ReviewDAO.exists(phone, productId)) {
            return false;
        }

        return ReviewDAO.update(review);
    }

    /**
     * Xóa đánh giá (khách hàng xóa đánh giá hoặc Admin gỡ bình luận vi phạm).
     */
    public static boolean delete(String customerPhone, String productId) throws SQLException {
        if (isNullOrEmpty(customerPhone) || isNullOrEmpty(productId)) {
            return false;
        }
        return ReviewDAO.delete(customerPhone.trim(), productId.trim());
    }

    /**
     * Kiểm tra khách hàng đã từng đánh giá sản phẩm hay chưa.
     */
    public static boolean hasReviewed(String customerPhone, String productId) throws SQLException {
        if (isNullOrEmpty(customerPhone) || isNullOrEmpty(productId)) {
            return false;
        }
        return ReviewDAO.exists(customerPhone.trim(), productId.trim());
    }

    /**
     * Lấy các đánh giá của một biến thể sản phẩm, mới nhất trước.
     */
    public static List<Review> getByProduct(String productId) throws SQLException {
        if (isNullOrEmpty(productId)) {
            return new ArrayList<>();
        }
        return ReviewDAO.findByProduct(productId.trim());
    }

    /**
     * Lấy tất cả đánh giá của cả nhóm sản phẩm (tổng hợp từ mọi biến thể), mới nhất trước.
     */
    public static List<Review> getByGroup(String groupId) throws SQLException {
        if (isNullOrEmpty(groupId)) {
            return new ArrayList<>();
        }
        return ReviewDAO.findByGroup(groupId.trim());
    }

    /**
     * Điểm đánh giá trung bình của một biến thể sản phẩm (0 nếu chưa có).
     */
    public static double getAverageRating(String productId) throws SQLException {
        if (isNullOrEmpty(productId)) {
            return 0.0;
        }
        return ReviewDAO.getAverageRating(productId.trim());
    }

    /**
     * Điểm đánh giá trung bình của cả nhóm sản phẩm (0 nếu chưa có).
     */
    public static double getAverageRatingByGroup(String groupId) throws SQLException {
        if (isNullOrEmpty(groupId)) {
            return 0.0;
        }
        return ReviewDAO.getAverageRatingByGroup(groupId.trim());
    }

    /**
     * Kiểm tra khách đã mua sản phẩm (đơn Hoàn thành chứa productId).
     */
    public static boolean hasPurchased(String customerPhone, String productId) throws SQLException {
        if (isNullOrEmpty(customerPhone) || isNullOrEmpty(productId)) {
            return false;
        }
        return ReviewDAO.hasPurchased(customerPhone.trim(), productId.trim());
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
