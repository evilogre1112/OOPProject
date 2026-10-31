package com.oop.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.oop.DAO.AccountDAO;
import com.oop.Extension.Extension;
import com.oop.DAO.DBContext;
import com.oop.Model.Account;
import com.oop.Model.User;
import com.oop.EXCEPTION.PermissionException;

public class AccountService {
    private Account currentAccount;

    private static String emailForm = "^[A-Za-z0-9][A-Za-z0-9_+-]*(\\.[A-Za-z0-9_+-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$";
    private static String phoneNumForm = "^0\\d{9}$";
    private static String passwordForm = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*]).{12,}$";

    /** Trả về tài khoản đang đăng nhập (null nếu chưa đăng nhập). */
    public Account getCurrentAccount() {
        return currentAccount == null ? null : currentAccount.clone();
    }

    /**
     * Đăng nhập: hash mật khẩu rồi so khớp CSDL, từ chối nếu status = false (bị
     * khóa). Thành công thì lưu vào currentAccount.
     */
    public Account login(String email, String password) throws SQLException {
        if (email == null || password == null) {
            throw new SQLException("Chưa nhập tài khoản hoặc mật khẩu");
        }
        Account account = AccountDAO.findByEmail(email);
        if (account == null) {
            throw new SQLException("Tài khoản không tồn tại");
        }
        if (!account.isStatus()) {
            throw new SQLException("Tài khoản đang bị khoá");
        }

        password = Extension.hash(password);

        if (!password.equals(account.getPassword())) {
            throw new SQLException("Mật khẩu không đúng");
        }
        currentAccount = account;
        return currentAccount.clone();
    }

    /** Đăng xuất: xóa currentAccount và giỏ hàng. */
    public void logout() {
        currentAccount = null;

    }

    /**
     * Đăng ký khách hàng: kiểm tra định dạng, trùng email/SĐT, hash mật khẩu, thêm
     * vào bảng Account + User/Customer.
     * Mặc định phải chưa có từ trước mật khẩu đã hash 
     */
    public static boolean registerForCustomer(Account account, boolean gender)
            throws SQLException, PermissionException {

        if (account == null)
            return false;

        String error = validate(
            account.getEmail(),
            account.getPhoneNum(),
            account.getPassword()
        );

        if (error != null)
            return false;

        account.setRole("Customer");

        return AccountDAO.createTransaction(account, gender, "Customer");
    }

    /** Đổi mật khẩu của tài khoản đang đăng nhập (phải đúng mật khẩu cũ). */
    // Đầu vào old password là cái choá gì , là raw hay là hash password -.-
    public boolean changePassword(String oldPassword, String newPassword) throws SQLException {
        if (currentAccount == null || oldPassword == null || newPassword == null) {
            return false;
        }
        if (!Extension.hash(oldPassword).equals(currentAccount.getPassword()))
            return false;
        if(!AccountDAO.updatePassword(currentAccount.getEmail(), Extension.hash(newPassword))) return false;
        currentAccount.setPassword(Extension.hash(newPassword));
        return true;

    }

    /** [Admin] Reset mật khẩu cho tài khoản bất kỳ. */
    public boolean resetPassword(String email, String newPassword) throws SQLException, PermissionException {
        if (currentAccount == null || !"Admin".equals(currentAccount.getRole())) {
            throw new PermissionException();
        }
        String hashed = Extension.hash(newPassword);
        boolean success = AccountDAO.updatePassword(email, hashed);

        if (success && email.equals(currentAccount.getEmail()))
            currentAccount.setPassword(hashed);

        return success;
    }

    /** Lấy hồ sơ (User: SĐT, giới tính) theo SĐT. */
    public static User getProfile(String phoneNum) throws SQLException {
        return AccountDAO.findUserByPhone(phoneNum);
    }

    /** Cập nhật hồ sơ: nickname (Account) và giới tính (User). */
   public boolean updateProfile(String email, String nickName, boolean gender)throws SQLException, PermissionException {

        if (currentAccount == null ||
            (!"Admin".equals(currentAccount.getRole()) &&
            !currentAccount.getEmail().equals(email))) {
            throw new PermissionException();
        }

        boolean success = AccountDAO.updateProfileTransaction(
            email, nickName, gender
        );

        if (success && currentAccount.getEmail().equals(email))
            currentAccount.setNickName(nickName);

        return success;
    }

    /** [Admin] Lấy danh sách toàn bộ tài khoản. */
    public List<Account> getAllAccounts() throws SQLException, PermissionException {
        if (currentAccount == null || !"Admin".equals(currentAccount.getRole())) {
            throw new PermissionException();
        }
        return AccountDAO.findAll();
    }

    /** [Admin] Khóa (false) hoặc mở khóa (true) tài khoản. */
    public boolean setAccountStatus(String email, boolean status) throws SQLException, PermissionException {
        if (currentAccount == null || !"Admin".equals(currentAccount.getRole())) {
            throw new PermissionException();
        }
        return AccountDAO.updateStatus(email, status);
    }

    /** [Admin] Tạo tài khoản Admin mới. */
    /** account truyền vào không được hash mật khẩu */
    public  boolean createAdmin(Account account, boolean gender) throws SQLException, PermissionException {
        if (account == null)return false;
        if (currentAccount == null || !"Admin".equals(currentAccount.getRole()) || !account.getRole().equals("Admin"))
            throw new PermissionException();
        if (AccountDAO.findByEmail(account.getEmail()) != null)
            throw new SQLException("Không được đổi quyền tài khoản này"); /* Tạo tài khoản admin mới không được đổi quyền */

        String error = validate(
                account.getEmail(),
                account.getPhoneNum(),
                account.getPassword()
            );

        if (error != null) {
            return false;
        }
        return AccountDAO.createTransaction(account, gender,"Admin");
    }

    /** Kiểm tra email/SĐT đã tồn tại chưa (dùng cho đăng ký và tạo admin). */
    private static boolean isExists(String email, String phoneNum) throws SQLException {
        if (AccountDAO.findByEmail(email) != null || AccountDAO.findUserByPhone(phoneNum) != null)
            return false;
        return true;
    }

    /**
     * Kiểm tra định dạng email, SĐT, mật khẩu hợp lệ; trả về thông báo lỗi hoặc
     * null nếu hợp lệ.
     */
    private static String validate(
            String email, String phoneNum, String password) {

        if (email == null || !email.matches(emailForm))
            return "Email không đúng định dạng";

        if (phoneNum == null || !phoneNum.matches(phoneNumForm))
            return "Số điện thoại không hợp lệ";

        if (password == null || !password.matches(passwordForm))
            return "Mật khẩu không hợp lệ";

        return null;
    }
}
