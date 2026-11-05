package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.Model.Address;

/**
 * Bảng Address: khóa (account_Email, Ordinal_num).
 * Cột: Note, Street, District, City, Is_default, Ordinal_num, account_Email.
 */
public class AddressDAO {

    private static final String COLUMNS = "Note, Street, District, City, Is_default, Ordinal_num, account_Email";

    /** Lấy toàn bộ địa chỉ của một tài khoản, sắp theo Ordinal_num. Không có thì trả list rỗng. */
    public static List<Address> findByEmail(String email) throws SQLException {
        List<Address> result = new ArrayList<>();
        String query = "SELECT " + COLUMNS + " FROM Address WHERE account_Email = ? ORDER BY Ordinal_num ASC";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }
        return result;
    }

    /** Lấy địa chỉ mặc định (Is_default = 1) của tài khoản. Không có thì trả null. */
    public static Address findDefault(String email) throws SQLException {
        String query = "SELECT " + COLUMNS + " FROM Address "
                + "WHERE account_Email = ? AND Is_default = 1 ORDER BY Ordinal_num ASC LIMIT 1";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    /** Lấy số thứ tự tiếp theo cho địa chỉ mới (MAX(Ordinal_num) + 1, bắt đầu từ 1). */
    public static int getNextOrdinal(String email) throws SQLException {
        String query = "SELECT COALESCE(MAX(Ordinal_num), 0) + 1 AS Next FROM Address WHERE account_Email = ?";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Next");
                }
            }
        }
        return 1;
    }

    /** Thêm địa chỉ mới (ordinalNum đã được service/getNextOrdinal gán sẵn). */
    public static boolean insert(Address address) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            return insert(conn, address);
        }
    }

    /**
     * Thêm địa chỉ và đặt nó làm mặc định trong MỘT transaction:
     * bỏ cờ mặc định cũ rồi chèn dòng mới với Is_default = 1.
     */
    public static boolean insertAsDefault(Address address) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                clearDefault(conn, address.getAccountEmail());
                address.setDefault(true);
                if (!insert(conn, address)) {
                    throw new SQLException("Không thể thêm địa chỉ");
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    /** Sửa địa chỉ theo khóa (account_Email, Ordinal_num). Không đổi cờ mặc định. */
    public static boolean update(Address address) throws SQLException {
        String query = "UPDATE Address SET Note = ?, Street = ?, District = ?, City = ? "
                + "WHERE account_Email = ? AND Ordinal_num = ?";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, address.getNote());
            ps.setString(2, address.getStreet());
            ps.setString(3, address.getDistrict());
            ps.setString(4, address.getCity());
            ps.setString(5, address.getAccountEmail());
            ps.setInt(6, address.getOrdinalNum());
            return ps.executeUpdate() == 1;
        }
    }

    /** Xóa địa chỉ theo khóa (account_Email, Ordinal_num). */
    public static boolean delete(String email, int ordinalNum) throws SQLException {
        String query = "DELETE FROM Address WHERE account_Email = ? AND Ordinal_num = ?";
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setInt(2, ordinalNum);
            return ps.executeUpdate() == 1;
        }
    }

    /** Bỏ cờ mặc định của tất cả địa chỉ thuộc tài khoản (Is_default = 0). Không có dòng nào cũng coi là thành công. */
    public static boolean clearDefault(String email) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            clearDefault(conn, email);
            return true;
        }
    }

    /** Đặt một địa chỉ làm mặc định (Is_default = 1). Chỉ dùng riêng lẻ; muốn đổi mặc định an toàn hãy dùng switchDefault. */
    public static boolean setDefault(String email, int ordinalNum) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            return setDefault(conn, email, ordinalNum);
        }
    }

    /**
     * Đổi địa chỉ mặc định trong MỘT transaction: bỏ mặc định cũ + đặt mặc định mới.
     * Lỗi ở bước nào cũng rollback, tài khoản không bao giờ bị mất địa chỉ mặc định.
     */
    public static boolean switchDefault(String email, int ordinalNum) throws SQLException {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                clearDefault(conn, email);
                if (!setDefault(conn, email, ordinalNum)) {
                    throw new SQLException("Không đặt được địa chỉ mặc định");
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        }
    }

    // ===== Helper (dùng chung Connection cho transaction) =====

    private static boolean insert(Connection conn, Address address) throws SQLException {
        String query = "INSERT INTO Address (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, address.getNote());
            ps.setString(2, address.getStreet());
            ps.setString(3, address.getDistrict());
            ps.setString(4, address.getCity());
            ps.setBoolean(5, address.isDefault());
            ps.setInt(6, address.getOrdinalNum());
            ps.setString(7, address.getAccountEmail());
            return ps.executeUpdate() == 1;
        }
    }

    private static void clearDefault(Connection conn, String email) throws SQLException {
        String query = "UPDATE Address SET Is_default = 0 WHERE account_Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    private static boolean setDefault(Connection conn, String email, int ordinalNum) throws SQLException {
        String query = "UPDATE Address SET Is_default = 1 WHERE account_Email = ? AND Ordinal_num = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setInt(2, ordinalNum);
            return ps.executeUpdate() == 1;
        }
    }

    private static Address map(ResultSet rs) throws SQLException {
        Address a = new Address();
        a.setNote(rs.getString("Note"));
        a.setStreet(rs.getString("Street"));
        a.setDistrict(rs.getString("District"));
        a.setCity(rs.getString("City"));
        a.setDefault(rs.getBoolean("Is_default"));
        a.setOrdinalNum(rs.getInt("Ordinal_num"));
        a.setAccountEmail(rs.getString("account_Email"));
        return a;
    }

    private static void rollback(Connection conn, Exception original) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }
}