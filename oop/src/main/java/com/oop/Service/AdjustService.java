package com.oop.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.oop.Model.Adjust;

public class AdjustService {

    /** Ghi nhật ký cho Admin đang đăng nhập (lấy SĐT từ AccountService.getCurrentAccount()). typeAdjust: "Thêm"/"Sửa"/"Xóa"; typeObject: "Sản phẩm"/"Danh mục"...; createdAt lấy giờ hiện tại. */
    public static void log(String typeAdjust, String objectName, String typeObject, String objectId) throws SQLException {
    }

    /** [Admin] Lấy toàn bộ nhật ký, mới nhất trước. */
    public static List<Adjust> getAll() throws SQLException {
        return null;
    }

    /** [Admin] Lọc nhật ký theo SĐT admin và khoảng ngày. Tham số null = bỏ qua. */
    public static List<Adjust> filter(String adminPhone, LocalDate from, LocalDate to) throws SQLException {
        return null;
    }
}
