package com.oop.Service;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Address;

public class AddressService {

    /** Lấy sổ địa chỉ của một tài khoản. */
    public static List<Address> getByEmail(String email) throws SQLException {
        return null;
    }

    /** Thêm địa chỉ: ordinalNum tự tăng theo tài khoản; địa chỉ đầu tiên tự là mặc định. */
    public static boolean add(Address address) throws SQLException {
        return false;
    }

    /** Sửa địa chỉ theo khóa (accountEmail, ordinalNum). */
    public static boolean update(Address address) throws SQLException {
        return false;
    }

    /** Xóa địa chỉ theo khóa (accountEmail, ordinalNum). */
    public static boolean delete(String email, int ordinalNum) throws SQLException {
        return false;
    }

    /** Đặt địa chỉ mặc định: bỏ mặc định cũ, đặt mặc định mới (đảm bảo chỉ có 1). */
    public static boolean setDefault(String email, int ordinalNum) throws SQLException {
        return false;
    }

    /** Lấy địa chỉ mặc định của tài khoản (dùng khi đặt hàng). */
    public static Address getDefault(String email) throws SQLException {
        return null;
    }
}
