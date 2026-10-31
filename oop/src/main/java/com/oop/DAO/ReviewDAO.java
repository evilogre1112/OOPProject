package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.oop.Model.Review;

public class ReviewDAO {

    /** Thêm đánh giá (khóa: Customer_Phone_num + Products_Id). */
    public static boolean insert(Review review) throws SQLException {
        String queryUpdate = "INSERT INTO Review (Customer_Phone_num, Products_Id, Created_at, Rating, Comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, review.getCustomerPhoneNum());
            preparedStatement.setString(2, review.getProductId());
            LocalDateTime createdAt = review.getCreatedAt() != null ? review.getCreatedAt() : LocalDateTime.now();
            preparedStatement.setTimestamp(3, Timestamp.valueOf(createdAt));
            preparedStatement.setInt(4, review.getRating());
            preparedStatement.setString(5, review.getComment());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    /** Sửa rating/comment của đánh giá theo khóa. */
    public static boolean update(Review review) throws SQLException {
        String queryUpdate = "UPDATE Review SET Rating = ?, Comment = ? WHERE Customer_Phone_num = ? AND Products_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setInt(1, review.getRating());
            preparedStatement.setString(2, review.getComment());
            preparedStatement.setString(3, review.getCustomerPhoneNum());
            preparedStatement.setString(4, review.getProductId());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    /** Xóa đánh giá (Dành cho Admin quản lý bình luận hoặc Khách hàng hủy đánh giá). */
    public static boolean delete(String customerPhone, String productId) throws SQLException {
        String queryUpdate = "DELETE FROM Review WHERE Customer_Phone_num = ? AND Products_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, customerPhone);
            preparedStatement.setString(2, productId);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    /** Kiểm tra khách đã đánh giá sản phẩm này chưa. */
    public static boolean exists(String customerPhone, String productId) throws SQLException {
        String query = "SELECT 1 FROM Review WHERE Customer_Phone_num = ? AND Products_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, customerPhone);
            preparedStatement.setString(2, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /** Giữ chữ ký gốc 2 tham số của project, mặc định kiểm tra đơn "Hoàn thành". */
    public static boolean hasPurchased(String customerPhone, String productId) throws SQLException {
        return hasPurchased(customerPhone, productId, "Hoàn thành");
    }

    /** Overload cho phép truyền trạng thái đơn hàng tùy chỉnh. */
    public static boolean hasPurchased(String customerPhone, String productId, String completedStatus) throws SQLException {
        String status = (completedStatus != null && !completedStatus.trim().isEmpty()) ? completedStatus : "Hoàn thành";
        String query = "SELECT 1 FROM Orders o "
                + "INNER JOIN Other_detail od ON o.Id = od.Orders_Id "
                + "WHERE o.Customer_Phone_num = ? AND od.Products_Id = ? AND o.Order_status = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, customerPhone);
            preparedStatement.setString(2, productId);
            preparedStatement.setString(3, status);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /** Lấy các đánh giá của một biến thể, mới nhất trước. */
    public static List<Review> findByProduct(String productId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Review WHERE Products_Id = ? ORDER BY Created_at DESC";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Review review = new Review();
                    review.setCustomerPhoneNum(resultSet.getString("Customer_Phone_num"));
                    review.setProductId(resultSet.getString("Products_Id"));
                    Timestamp ts = resultSet.getTimestamp("Created_at");
                    review.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                    review.setRating(resultSet.getInt("Rating"));
                    review.setComment(resultSet.getString("Comment"));
                    reviews.add(review);
                }
            }
        }
        return reviews;
    }

    /** Lấy các đánh giá của cả nhóm sản phẩm (JOIN Products), mới nhất trước. */
    public static List<Review> findByGroup(String groupId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT r.Customer_Phone_num, r.Products_Id, r.Created_at, r.Rating, r.Comment "
                + "FROM Review r "
                + "INNER JOIN Products p ON r.Products_Id = p.Id "
                + "WHERE p.Group_Product_Id = ? "
                + "ORDER BY r.Created_at DESC";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Review review = new Review();
                    review.setCustomerPhoneNum(resultSet.getString("Customer_Phone_num"));
                    review.setProductId(resultSet.getString("Products_Id"));
                    Timestamp ts = resultSet.getTimestamp("Created_at");
                    review.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                    review.setRating(resultSet.getInt("Rating"));
                    review.setComment(resultSet.getString("Comment"));
                    reviews.add(review);
                }
            }
        }
        return reviews;
    }

    /** Điểm trung bình của một biến thể (dùng COALESCE trả về 0 nếu chưa có đánh giá). */
    public static double getAverageRating(String productId) throws SQLException {
        String query = "SELECT COALESCE(AVG(Rating), 0) AS AvgRating FROM Review WHERE Products_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("AvgRating");
                }
            }
        }
        return 0;
    }

    /** Điểm trung bình của cả nhóm sản phẩm (dùng COALESCE trả về 0 nếu chưa có đánh giá). */
    public static double getAverageRatingByGroup(String groupId) throws SQLException {
        String query = "SELECT COALESCE(AVG(r.Rating), 0) AS AvgRating "
                + "FROM Review r "
                + "INNER JOIN Products p ON r.Products_Id = p.Id "
                + "WHERE p.Group_Product_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("AvgRating");
                }
            }
        }
        return 0;
    }
}