package com.oop.DAO;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Address;

public class AddressDAO {

    /** Lấy toàn bộ địa chỉ của một tài khoản, sắp theo Ordinal_num. */
    public static List<Address> findByEmail(String email) throws SQLException {
        return null;
    }

    /** Lấy địa chỉ mặc định (Is_default = 1) của tài khoản. Không có thì trả null. */
    public static Address findDefault(String email) throws SQLException {
        return null;
    }

    /** Lấy số thứ tự tiếp theo cho địa chỉ mới (MAX(Ordinal_num) + 1, bắt đầu từ 1). */
    public static int getNextOrdinal(String email) throws SQLException {
        return 0;
    }

    /** Thêm địa chỉ mới (ordinalNum đã được service/getNextOrdinal gán sẵn). */
    public static boolean insert(Address address) throws SQLException {
        return false;
    }

    /** Sửa địa chỉ theo khóa (account_Email, Ordinal_num). */
    public static boolean update(Address address) throws SQLException {
        return false;
    }

    /** Xóa địa chỉ theo khóa (account_Email, Ordinal_num). */
    public static boolean delete(String email, int ordinalNum) throws SQLException {
        return false;
    }

    /** Bỏ cờ mặc định của tất cả địa chỉ thuộc tài khoản (Is_default = 0). */
    public static boolean clearDefault(String email) throws SQLException {
        return false;
    }

    /** Đặt một địa chỉ làm mặc định (Is_default = 1). Service gọi clearDefault trước. */
    public static boolean setDefault(String email, int ordinalNum) throws SQLException {
        return false;
    }
}
