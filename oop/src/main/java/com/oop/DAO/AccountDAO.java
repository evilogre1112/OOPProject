package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.Model.Account;
import com.oop.Model.User;
import com.oop.Extension.Extension;
import com.oop.EXCEPTION.PermissionException;
import com.oop.EXCEPTION.SQL;

public class AccountDAO {
    private static Connection databaseConnection;

    static {
        try {
            databaseConnection = DBContext.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối Database", e);
        }
    }

    /**
     * Tìm tài khoản theo email (kèm mật khẩu đã hash để service so khớp). Không có
     * thì trả null.
     */
    public static synchronized Account findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM Account WHERE email = ?";

        try (
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Account acc = new Account();
                    acc.setEmail(resultSet.getString("email"));
                    acc.setPhoneNum(resultSet.getString("phone_num"));
                    acc.setPassword(resultSet.getString("password"));
                    acc.setNickName(resultSet.getString("nickname"));
                    acc.setRole(resultSet.getString("role"));
                    acc.setStatus(resultSet.getBoolean("status"));
                    return acc;
                }
            }
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
        return null;
    }

    /** Kiểm tra email đã tồn tại trong bảng Account chưa. */
    public static synchronized boolean existsEmail(String email) throws SQLException {
        if (findByEmail(email) == null)
            return false;
        return true;
    }

    /** Kiểm tra SĐT đã tồn tại trong bảng User chưa. */
    public static synchronized boolean existsPhone(String phoneNum) throws SQLException {
        String query = "SELECT * FROM User WHERE Phone_num = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, phoneNum);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
        return false;
    }

    /**
     * Thêm dòng vào bảng User (Phone_num, Gender). Dùng chung transaction khi đăng
     * ký.
     */
    public static synchronized boolean insertUser(User user) throws SQLException {
        String query = "INSERT INTO User (Phone_num, Gender) VALUES (?, ?)";

        try (PreparedStatement ps = databaseConnection.prepareStatement(query)) {
            ps.setString(1, user.getPhoneNum());
            ps.setBoolean(2, user.getGender());

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Thêm dòng vào bảng Customer (phải insertUser trước). */
    public static synchronized boolean insertCustomer(String phoneNum) throws SQLException {
        if (!existsPhone(phoneNum))
            return false;
        String queryUpdate = "INSERT INTO Customer (Phone_num) VALUES (?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, phoneNum);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Thêm dòng vào bảng Admin (phải insertUser trước). */
    public static synchronized boolean insertAdmin(String phoneNum) throws SQLException {
        if (!existsPhone(phoneNum))
            return false;
        String queryUpdate = "INSERT INTO Admin (Phone_num) VALUES (?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, phoneNum);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /**
     * Thêm dòng vào bảng Account (mật khẩu đã được hash ở service; phải insertUser
     * trước vì có FK Phone_num).
     */
    public static synchronized boolean insertAccount(Account account) throws SQLException {
        if (!existsPhone(account.getPhoneNum()))
            return false; // account không chứa thông tin gender nên thoát ra phải tạo 1 hàm khởi tạo
                          // riêng
        String queryUpdate = "INSERT INTO Account (Email, Password, NickName, Role, Status,Phone_num) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, account.getEmail());
            preparedStatement.setString(2, Extension.hash(account.getPassword()));
            preparedStatement.setString(3, account.getNickName());
            preparedStatement.setString(4, account.getRole());
            preparedStatement.setBoolean(5, account.isStatus());
            preparedStatement.setString(6, account.getPhoneNum());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Cập nhật mật khẩu (đã hash) theo email. */
    public static synchronized boolean updatePassword(String email, String hashedPassword) throws SQLException {
        String queryUpdate = "UPDATE Account SET Password = ? WHERE Email = ?";
        try (
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setString(2, email);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Cập nhật nickname theo email. */
    public static synchronized boolean updateNickName(String email, String nickName) throws SQLException {
        String queryUpdate = "UPDATE Account SET NickName = ? WHERE Email = ?";
        try (
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setString(1, nickName);
            preparedStatement.setString(2, email);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Cập nhật giới tính trong bảng User theo SĐT. */
    public static synchronized boolean updateGender(String phoneNum, boolean gender) throws SQLException {
        String queryUpdate = "UPDATE User SET Gender = ? WHERE Phone_num = ?";
        try (
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setBoolean(1, gender);
            preparedStatement.setString(2, phoneNum);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Lấy User (SĐT, giới tính) theo SĐT. Không có thì trả null. */
    public static synchronized User findUserByPhone(String phoneNum) throws SQLException {
        String query = "SELECT * FROM User WHERE Phone_num = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, phoneNum);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User();
                    user.setPhoneNum(resultSet.getString("Phone_num"));
                    user.setGender(resultSet.getBoolean("Gender"));
                    return user;
                }
            }
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
        return null;
    }

    /** Lấy toàn bộ tài khoản (dùng cho Admin quản lý người dùng). */
    public static synchronized List<Account> findAll() throws SQLException {
        String query = "SELECT Email, NickName, Role, Status FROM Account";
        try (
                PreparedStatement preparedStatement = databaseConnection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            List<Account> accounts = new ArrayList<>();
            while (resultSet.next()) {
                Account acc = new Account();
                acc.setEmail(resultSet.getString("Email"));
                acc.setNickName(resultSet.getString("NickName"));
                acc.setRole(resultSet.getString("Role"));
                acc.setStatus(resultSet.getBoolean("Status"));
                accounts.add(acc);
            }
            return accounts;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Cập nhật Status của tài khoản: true = hoạt động, false = bị khóa. */
    public static synchronized boolean updateStatus(String email, boolean status) throws SQLException {
        String queryUpdate = "UPDATE Account SET Status = ? WHERE Email = ?";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setBoolean(1, status);
            preparedStatement.setString(2, email);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }
    /**Thêm một lúc cả tài khoản, User, quyền */ 
    public static synchronized boolean createTransaction(Account account, boolean gender, String Role) throws SQLException,PermissionException {
        if(Role != "Customer" && Role != "Admin") throw new PermissionException();
        if (!databaseConnection.getAutoCommit())
            throw new SQLException("Connection đang có transaction khác");

        databaseConnection.setAutoCommit(false);

        try {
            if (!existsPhone(account.getPhoneNum())) {
                if (!insertUser(new User(account.getPhoneNum(), gender)))
                    throw new SQLException("Không thể thêm User");
            }
            if (!insertAccount(account))
                throw new SQLException("Không thể thêm Account");
            if(Role == "Admin"){
                if (!insertAdmin(account.getPhoneNum()))
                    throw new SQLException("Không thể thêm Admin");
            }
            else{
                if (!insertCustomer(account.getPhoneNum()))
                    throw new SQLException("Không thể thêm Customer");
            }

            databaseConnection.commit();
            return true;

        } catch (SQLException | RuntimeException e) {
            try {
                databaseConnection.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            databaseConnection.setAutoCommit(true);
        }
    }

    public static synchronized boolean updateProfileTransaction(String email, String nickName, boolean gender)throws SQLException {

        if (!databaseConnection.getAutoCommit())
            throw new SQLException("Connection đang có transaction khác");

        Account account = findByEmail(email);

        if (account == null)
            return false;

        User user = findUserByPhone(account.getPhoneNum());

        if (user == null)
            return false;

        databaseConnection.setAutoCommit(false);
        boolean restoreConnection = false;

        try {
            // Chỉ UPDATE khi nickname thay đổi
            if (!java.util.Objects.equals(account.getNickName(), nickName)) {
                if (!updateNickName(email, nickName))
                    throw new SQLException("Không thể cập nhật NickName");
            }

            // Chỉ UPDATE khi giới tính thay đổi
            if (user.getGender() != gender) {
                if (!updateGender(account.getPhoneNum(), gender))
                    throw new SQLException("Không thể cập nhật Gender");
            }

            databaseConnection.commit();
            restoreConnection = true;
            return true;

        } catch (SQLException | RuntimeException e) {
            try {
                databaseConnection.rollback();
                restoreConnection = true;
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }

            throw e;

        } finally {
            if (restoreConnection)
                databaseConnection.setAutoCommit(true);
        }
    }
}
