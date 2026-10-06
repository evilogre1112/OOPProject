package com.oop.Service;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Account;
import com.oop.Model.User;

public class AccountService {

    private static Account currentAccount;

    /** Trả về tài khoản đang đăng nhập (null nếu chưa đăng nhập). */
    public static Account getCurrentAccount() {
        return null;
    }

    /** Đăng nhập: hash mật khẩu rồi so khớp CSDL, từ chối nếu status = false (bị khóa). Thành công thì lưu vào currentAccount. */
    public static Account login(String email, String password) throws SQLException {
        return null;
    }

    /** Đăng xuất: xóa currentAccount và giỏ hàng. */
    public static void logout() {
    }

    /** Đăng ký khách hàng: kiểm tra định dạng, trùng email/SĐT, hash mật khẩu, thêm vào bảng Account + User/Customer. */
    public static boolean register(Account account, String rawPassword, boolean gender) throws SQLException {
        return false;
    }

    /** Đổi mật khẩu của tài khoản đang đăng nhập (phải đúng mật khẩu cũ). */
    public static boolean changePassword(String oldPassword, String newPassword) throws SQLException {
        return false;
    }

    /** [Admin] Reset mật khẩu cho tài khoản bất kỳ. */
    public static boolean resetPassword(String email, String newPassword) throws SQLException {
        return false;
    }

    /** Lấy hồ sơ (User: SĐT, giới tính) theo SĐT. */
    public static User getProfile(String phoneNum) throws SQLException {
        return null;
    }

    /** Cập nhật hồ sơ: nickname (Account) và giới tính (User). */
    public static boolean updateProfile(String email, String nickName, boolean gender) throws SQLException {
        return false;
    }

    /** [Admin] Lấy danh sách toàn bộ tài khoản. */
    public static List<Account> getAllAccounts() throws SQLException {
        return null;
    }

    /** [Admin] Khóa (false) hoặc mở khóa (true) tài khoản. */
    public static boolean setAccountStatus(String email, boolean status) throws SQLException {
        return false;
    }

    /** [Admin] Tạo tài khoản Admin mới. */
    public static boolean createAdmin(Account account, String rawPassword, boolean gender) throws SQLException {
        return false;
    }

    /** Băm mật khẩu bằng SHA-256. */
    private static String hash(String rawPassword) {
        return null;
    }

    /** Kiểm tra email/SĐT đã tồn tại chưa (dùng cho đăng ký và tạo admin). */
    private static boolean isExists(String email, String phoneNum) throws SQLException {
        return false;
    }

    /** Kiểm tra định dạng email, SĐT, mật khẩu hợp lệ; trả về thông báo lỗi hoặc null nếu hợp lệ. */
    private static String validate(String email, String phoneNum, String password) {
        return null;
    }
}
