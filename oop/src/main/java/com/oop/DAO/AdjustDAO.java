package com.oop.DAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.oop.Model.Adjust;

public class AdjustDAO {

    /** Thêm một dòng nhật ký (Id tự tăng, Created_at mặc định CURRENT_TIMESTAMP nên không cần insert). */
    public static boolean insert(Adjust adjust) throws SQLException {
        return false;
    }

    /** Lấy toàn bộ nhật ký, mới nhất trước. */
    public static List<Adjust> findAll() throws SQLException {
        return null;
    }

    /** Lọc nhật ký theo SĐT Admin và khoảng ngày Created_at. Tham số null = bỏ qua. */
    public static List<Adjust> filter(String adminPhone, LocalDate from, LocalDate to) throws SQLException {
        return null;
    }
}
