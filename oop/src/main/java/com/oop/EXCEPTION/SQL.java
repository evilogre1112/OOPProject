package com.oop.EXCEPTION;

import java.sql.SQLException;

public class SQL {

    public static SQLException Exception(SQLException e) {

        String message = switch (e.getErrorCode()) {

            // ===== RÀNG BUỘC DỮ LIỆU =====
            case 1062 -> "Dữ liệu đã tồn tại (trùng PRIMARY KEY hoặc UNIQUE)";
            case 1048 -> "Không được để NULL ở cột bắt buộc";
            case 1364 -> "Thiếu giá trị cho cột bắt buộc không có DEFAULT";
            case 1451 -> "Không thể xóa hoặc cập nhật vì dữ liệu đang được tham chiếu";
            case 1452 -> "Khóa ngoại không hợp lệ hoặc dữ liệu tham chiếu không tồn tại";
            case 3819 -> "Dữ liệu vi phạm điều kiện CHECK";

            // ===== KIỂU DỮ LIỆU / GIÁ TRỊ =====
            case 1406 -> "Dữ liệu vượt quá độ dài cho phép của cột";
            case 1264 -> "Giá trị nằm ngoài phạm vi kiểu dữ liệu";
            case 1265 -> "Dữ liệu bị cắt bớt khi chuyển đổi";
            case 1292 -> "Giá trị có định dạng hoặc kiểu dữ liệu không hợp lệ";
            case 1366 -> "Giá trị không phù hợp với kiểu dữ liệu của cột";
            case 1365 -> "Phép chia cho 0";
            case 1690 -> "Kết quả tính toán vượt quá phạm vi cho phép";

            // ===== CÚ PHÁP SQL =====
            case 1064 -> "Sai cú pháp SQL";
            case 1054 -> "Tên cột không tồn tại";
            case 1052 -> "Tên cột không rõ ràng trong truy vấn";
            case 1109 -> "Bảng được tham chiếu không tồn tại trong truy vấn";
            case 1110 -> "Một cột được chỉ định nhiều lần";
            case 1136 -> "Số lượng giá trị không khớp số lượng cột";
            case 1241 -> "Số lượng cột của toán hạng không hợp lệ";
            case 1242 -> "Truy vấn con trả về nhiều hơn một dòng";
            case 1235 -> "Chức năng SQL chưa được hỗ trợ";

            // ===== BẢNG / DATABASE =====
            case 1046 -> "Chưa chọn Database";
            case 1049 -> "Database không tồn tại";
            case 1050 -> "Bảng đã tồn tại";
            case 1051 -> "Bảng cần thao tác không tồn tại";
            case 1146 -> "Bảng không tồn tại";
            case 1091 -> "Không thể xóa cột hoặc khóa vì không tồn tại";
            case 1067 -> "Giá trị DEFAULT của cột không hợp lệ";
            case 1068 -> "Bảng có nhiều hơn một PRIMARY KEY";
            case 1071 -> "Độ dài khóa vượt quá giới hạn";
            case 1072 -> "Cột dùng trong khóa không tồn tại";

            // ===== QUYỀN TRUY CẬP =====
            case 1044 -> "Không có quyền truy cập Database";
            case 1045 -> "Tên đăng nhập hoặc mật khẩu MySQL không hợp lệ";
            case 1142 -> "Không có quyền thực hiện thao tác trên bảng";
            case 1143 -> "Không có quyền thực hiện thao tác trên cột";

            // ===== TRANSACTION / ĐỒNG THỜI =====
            case 1205 -> "Hết thời gian chờ khóa dữ liệu";
            case 1213 -> "Xảy ra Deadlock giữa các transaction";
            case 1412 -> "Cấu trúc bảng đã thay đổi, cần thực hiện lại transaction";

            // ===== KẾT NỐI / TÀI NGUYÊN =====
            case 1040 -> "MySQL đã đạt giới hạn số kết nối";
            case 1203 -> "Người dùng đã vượt quá giới hạn kết nối";
            case 1153 -> "Gói dữ liệu gửi đến MySQL quá lớn";
            case 1461 -> "Đã đạt giới hạn PreparedStatement";
            case 2002 -> "Không thể kết nối MySQL qua socket";
            case 2003 -> "Không thể kết nối đến máy chủ MySQL";
            case 2005 -> "Không tìm thấy địa chỉ máy chủ MySQL";
            case 2006 -> "Máy chủ MySQL đã ngắt kết nối";
            case 2013 -> "Mất kết nối MySQL trong lúc truy vấn";
            case 2014 -> "Các lệnh truy vấn thực hiện sai thứ tự";
            case 2020 -> "Gói dữ liệu MySQL quá lớn";

            // ===== FOREIGN KEY / CẤU TRÚC =====
            case 1822 -> "Khóa ngoại thiếu INDEX ở bảng được tham chiếu";
            case 1824 -> "Không thể mở bảng được tham chiếu bởi khóa ngoại";

            // ===== STORED PROCEDURE =====
            case 1305 -> "Stored Procedure hoặc Function không tồn tại";

            // ===== LỖI KHÁC =====
            default -> "Lỗi Database: " + e.getMessage();
        };

        // Xử lý các lỗi kết nối JDBC không trả mã lỗi MySQL
        if (e.getErrorCode() == 0
                && e.getSQLState() != null
                && e.getSQLState().startsWith("08")) {
            message = "Lỗi kết nối Database: " + e.getMessage();
        }

        return new SQLException(
            message,
            e.getSQLState(),
            e.getErrorCode(),
            e
        );
    }
}