package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.Model.User;

public class UserRepository {
    static public List<User> getAllUser() {
        ArrayList<User> users = new ArrayList<>();
        String sql = """
                SELECT * FROM User
                """;
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String phoneNum = rs.getString("Phone_num");
                boolean gender = rs.getBoolean("Gender");
                users.add(new User(phoneNum, gender));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
