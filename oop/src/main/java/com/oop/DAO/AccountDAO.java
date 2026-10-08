package com.oop.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Account;
import com.oop.Model.User;

public class AccountDAO {

    /** Tìm tài khoản theo email (kèm mật khẩu đã hash để service so khớp). Không có thì trả null. */
    public static Account findByEmail(String email) throws SQLException {
        return null;
    }

    /** Kiểm tra email đã tồn tại trong bảng Account chưa. */
    public static boolean existsEmail(String email) throws SQLException {
        return false;
    }

    /** Kiểm tra SĐT đã tồn tại trong bảng User chưa. */
    public static boolean existsPhone(String phoneNum) throws SQLException {
        return false;
    }

    /** Thêm dòng vào bảng User (Phone_num, Gender). Dùng chung transaction khi đăng ký. */
    public static boolean insertUser(Connection conn, User user) throws SQLException {
        return false;
    }

    /** Thêm dòng vào bảng Customer (phải insertUser trước). */
    public static boolean insertCustomer(Connection conn, String phoneNum) throws SQLException {
        return false;
    }

    /** Thêm dòng vào bảng Admin (phải insertUser trước). */
    public static boolean insertAdmin(Connection conn, String phoneNum) throws SQLException {
        return false;
    }

    /** Thêm dòng vào bảng Account (mật khẩu đã được hash ở service; phải insertUser trước vì có FK Phone_num). */
    public static boolean insertAccount(Connection conn, Account account) throws SQLException {
        return false;
    }

    /** Cập nhật mật khẩu (đã hash) theo email. */
    public static boolean updatePassword(String email, String hashedPassword) throws SQLException {
        return false;
    }

    /** Cập nhật nickname theo email. */
    public static boolean updateNickName(String email, String nickName) throws SQLException {
        return false;
    }

    /** Cập nhật giới tính trong bảng User theo SĐT. */
    public static boolean updateGender(String phoneNum, boolean gender) throws SQLException {
        return false;
    }

    /** Lấy User (SĐT, giới tính) theo SĐT. Không có thì trả null. */
    public static User findUserByPhone(String phoneNum) throws SQLException {
        return null;
    }

    /** Lấy toàn bộ tài khoản (dùng cho Admin quản lý người dùng). */
    public static List<Account> findAll() throws SQLException {
        return null;
    }

    /** Cập nhật Status của tài khoản: true = hoạt động, false = bị khóa. */
    public static boolean updateStatus(String email, boolean status) throws SQLException {
        return false;
    }
}
