package com.oop.DAO;

import java.sql.Connection;// kết nói đến cơ sở dữ liệu
import java.sql.PreparedStatement;// dùng gửi lệnh sql đến csdl
import java.sql.ResultSet;// dùng chứa bảng kết quả dùng duyệt hàng dữ liệu
import java.sql.Timestamp;// dùng chuyển đổi LocalDateTime sang Timestamp để insert vào csdl

import java.sql.SQLException;// dùng xử lý lỗi
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import com.oop.Model.Adjust;

public class AdjustDAO {

    /**
     * Thêm một dòng nhật ký (Id tự tăng, Created_at mặc định CURRENT_TIMESTAMP nên
     * không cần insert).
     */
    public static boolean insert(Adjust adjust) throws SQLException {
        String sql = """
                INSERT INTO Adjust(
                    Admin_Phone_num,
                    Type_adjust,
                    Object_name_adjusted,
                    Type_Object,
                    Id_Object_Adjusted)
                    VALUES(?,?,?,?,?)""";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, adjust.getAdminPhoneNum());
            ps.setString(2, adjust.getTypeAdjust());
            ps.setString(3, adjust.getObjectNameAdjusted());
            ps.setString(4, adjust.getTypeObject());
            ps.setString(5, adjust.getIdObjectAdjusted());
            return ps.executeUpdate() > 0;
        }
    }

    /** Lấy toàn bộ nhật ký, mới nhất trước. */
    public static List<Adjust> findAll() throws SQLException {
        List<Adjust> result = new ArrayList<>();
        String sql = """
                SELECT * FROM Adjust
                ORDER BY Created_at DESC, Id DESC""";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapResultSet(rs));
            }
        }
        return result;
    }

    /**
     * Lọc nhật ký theo SĐT Admin và khoảng ngày Created_at. Tham số null = bỏ qua.
     */
    public static List<Adjust> filter(String adminPhone, LocalDate from, LocalDate to) throws SQLException {

        List<Adjust> result = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM Adjust
                WHERE 1 = 1
                """);

        if (adminPhone != null && !adminPhone.trim().isEmpty()) {
            sql.append(" AND Admin_Phone_num = ?");
        }

        if (from != null) {
            sql.append(" AND Created_at >= ?");
        }

        if (to != null) {
            sql.append(" AND Created_at < ?");
        }

        sql.append(" ORDER BY Created_at DESC, Id DESC");

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (adminPhone != null && !adminPhone.trim().isEmpty()) {
                ps.setString(index++, adminPhone.trim());
            }

            if (from != null) {
                ps.setTimestamp(
                        index++,
                        Timestamp.valueOf(from.atStartOfDay()));
            }

            if (to != null) {
                ps.setTimestamp(
                        index++,
                        Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSet(rs));
                }
            }
        }

        return result;
    }

    /**
     * Ánh xạ dữ liệu ResultSet thành đối tượng Adjust.
     */
    private static Adjust mapResultSet(ResultSet rs) throws SQLException {

        Adjust adjust = new Adjust();

        adjust.setId(rs.getInt("Id"));
        adjust.setAdminPhoneNum(rs.getString("Admin_Phone_num"));

        Timestamp createdAt = rs.getTimestamp("Created_at");

        if (createdAt != null) {
            adjust.setCreatedAt(createdAt.toLocalDateTime());
        }

        adjust.setTypeAdjust(rs.getString("Type_adjust"));
        adjust.setObjectNameAdjusted(
                rs.getString("Object_name_adjusted"));
        adjust.setTypeObject(rs.getString("Type_Object"));
        adjust.setIdObjectAdjusted(
                rs.getString("Id_Object_Adjusted"));

        return adjust;
    }
}
