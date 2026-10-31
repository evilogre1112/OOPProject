package com.oop.EXCEPTION;

import java.sql.SQLException;

public class SQL {

    public static SQLException Exception(SQLException e) {
        switch (e.getErrorCode()) {
            case 1062:
                return new SQLException("Dữ liệu đã tồn tại", e);

            case 1452:
                return new SQLException("Dữ liệu khóa ngoại không hợp lệ", e);

            case 1048:
                return new SQLException("Không được để trống dữ liệu", e);

            case 1146:
                return new SQLException("Bảng không tồn tại", e);

            case 1054:
                return new SQLException("Tên cột không hợp lệ", e);

            default:
                return new SQLException("Lỗi Database: " + e.getMessage(), e);
        }
    }
}