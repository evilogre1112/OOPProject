package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.Model.Account;
import com.oop.Model.User;

public class AccountDAO {

    /** Tìm tài khoản theo email (kèm mật khẩu đã hash để service so khớp). Không có thì trả null. */
    public static Account findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM Account WHERE email = ?";
        
        try (Connection databaseConnection = DBContext.getConnection();
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
        }
        return null;
    }

    /** Kiểm tra email đã tồn tại trong bảng Account chưa. */
    public static boolean existsEmail(String email) throws SQLException {
        if(findByEmail(email) == null) return false;
        return true;
    }

    /** Kiểm tra SĐT đã tồn tại trong bảng User chưa. */
    public static boolean existsPhone(Connection databaseConnection,String phoneNum) throws SQLException {
        String query = "SELECT * FROM User WHERE Phone_num = ?";
        
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
                preparedStatement.setString(1, phoneNum);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
        }
        return false;
    }

    /** Thêm dòng vào bảng User (Phone_num, Gender). Dùng chung transaction khi đăng ký. */
    public static boolean insertUser(Connection databaseConnection, User user) throws SQLException {
        String queryUpdate = "INSERT INTO User (Phone_num, Gender) VALUES (?,?)";
        
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
                preparedStatement.setString(1, user.getPhoneNum());
                preparedStatement.setBoolean(2,user.getGender());
                return preparedStatement.executeUpdate() == 1; 
        }
        
    }

    /** Thêm dòng vào bảng Customer (phải insertUser trước). */
    public static boolean insertCustomer(Connection databaseConnection, String phoneNum) throws SQLException {
        if(!existsPhone(databaseConnection, phoneNum)) return false ;
        String queryUpdate = "INSERT INTO Customer (Phone_num) VALUES (?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
                preparedStatement.setString(1, phoneNum);
                return preparedStatement.executeUpdate() == 1;   
        }
    }

    /** Thêm dòng vào bảng Admin (phải insertUser trước). */
    public static boolean insertAdmin(Connection databaseConnection, String phoneNum) throws SQLException {
        if(!existsPhone(databaseConnection, phoneNum)) return false ;
        String queryUpdate = "INSERT INTO Admin (Phone_num) VALUES (?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
                preparedStatement.setString(1, phoneNum);
                return preparedStatement.executeUpdate() == 1;   
        }
    }

    /** Thêm dòng vào bảng Account (mật khẩu đã được hash ở service; phải insertUser trước vì có FK Phone_num). */
    public static boolean insertAccount(Connection databaseConnection, Account account) throws SQLException {
        if(!existsPhone(databaseConnection, account.getPhoneNum())) return false ; // account không chứa thông tin gender nên thoát ra phải tạo 1 hàm khởi tạo riêng
        String queryUpdate = "INSERT INTO Account (Email, Password, NickName, Role, Status,Phone_num) VALUES (?,?,?,?,?,?)";
         try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
                preparedStatement.setString(1, account.getEmail());
                preparedStatement.setString(2, account.getPassword());
                preparedStatement.setString(3, account.getNickName());
                preparedStatement.setString(4, account.getRole());
                preparedStatement.setBoolean(5, account.isStatus());
                preparedStatement.setString(6, account.getPhoneNum());
                return preparedStatement.executeUpdate() == 1;   
        }
    }

    /** Cập nhật mật khẩu (đã hash) theo email. */
    public static boolean updatePassword(String email, String hashedPassword) throws SQLException {
        String queryUpdate = "UPDATE Account SET Password = ? WHERE Email = ?";
         try (Connection databaseConnection = DBContext.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);){
            preparedStatement.setString(1,hashedPassword);
            preparedStatement.setString(2,email);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    /** Cập nhật nickname theo email. */
    public static boolean updateNickName(String email, String nickName) throws SQLException {
        String queryUpdate = "UPDATE Account SET NickName = ? WHERE Email = ?";
         try (Connection databaseConnection = DBContext.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);){
            preparedStatement.setString(1,nickName);
            preparedStatement.setString(2,email);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    /** Cập nhật giới tính trong bảng User theo SĐT. */
    public static boolean updateGender(String phoneNum, boolean gender) throws SQLException {
        String queryUpdate = "UPDATE User SET Gender = ? WHERE Phone_num = ?";
         try (Connection databaseConnection = DBContext.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);){
            preparedStatement.setBoolean(1,gender);
            preparedStatement.setString(2,phoneNum);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    /** Lấy User (SĐT, giới tính) theo SĐT. Không có thì trả null. */
    public static User findUserByPhone(String phoneNum) throws SQLException {
        String query = "SELECT * FROM User WHERE Phone_num = ?";
        
        try (Connection databaseConnection = DBContext.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
                preparedStatement.setString(1, phoneNum);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        User user = new User();
                        user.setPhoneNum(resultSet.getString("Phone_num"));
                        user.setGender(resultSet.getBoolean("Gender") );
                        return user;
                    }
                }
        }
        return null;
    }

    /** Lấy toàn bộ tài khoản (dùng cho Admin quản lý người dùng). */
    public static List<Account> findAll() throws SQLException {
        String query = "SELECT Email, NickName, Role, Status FROM Account";
        try (Connection databaseConnection = DBContext.getConnection();
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
        }
    }

    /** Cập nhật Status của tài khoản: true = hoạt động, false = bị khóa. */
    public static boolean updateStatus(String email, boolean status) throws SQLException {
        String queryUpdate = "UPDATE Account SET Status = ? WHERE Email = ?";
        try (Connection databaseConnection = DBContext.getConnection();
            PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);){
            preparedStatement.setBoolean(1,status);
            preparedStatement.setString(2,email);
            return preparedStatement.executeUpdate() == 1;
        }
    }
}
