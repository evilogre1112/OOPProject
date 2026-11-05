package com.oop.Service;

import java.sql.SQLException;
import java.util.List;

import com.oop.DAO.AddressDAO;
import com.oop.Model.Address;

public class AddressService {

    /** Lấy sổ địa chỉ của một tài khoản (không bao giờ trả null). */
    public static List<Address> getByEmail(String email) throws SQLException {
        if (isBlank(email))
            return List.of();
        List<Address> list = AddressDAO.findByEmail(email.trim());
        return list == null ? List.of() : list;
    }

    /** Thêm địa chỉ: ordinalNum tự tăng theo tài khoản; địa chỉ đầu tiên tự là mặc định. */
    public static boolean add(Address address) throws SQLException {
        if (!isValid(address))
            return false;
        normalize(address);

        String email = address.getAccountEmail();
        List<Address> existing = AddressDAO.findByEmail(email);
        boolean isFirst = existing == null || existing.isEmpty();
        boolean makeDefault = isFirst || address.isDefault();

        address.setOrdinalNum(AddressDAO.getNextOrdinal(email));
        address.setDefault(makeDefault);

        // Một transaction: bỏ mặc định cũ + chèn dòng mới là mặc định
        return makeDefault ? AddressDAO.insertAsDefault(address) : AddressDAO.insert(address);
    }

    /** Sửa địa chỉ theo khóa (accountEmail, ordinalNum). Không đổi cờ mặc định (dùng setDefault). */
    public static boolean update(Address address) throws SQLException {
        if (!isValid(address))
            return false;
        normalize(address);

        Address existing = findOne(address.getAccountEmail(), address.getOrdinalNum());
        if (existing == null)
            return false;

        address.setDefault(existing.isDefault());
        return AddressDAO.update(address);
    }

    /** Xóa địa chỉ theo khóa. Nếu xóa địa chỉ mặc định mà còn địa chỉ khác thì địa chỉ đầu tiên thành mặc định. */
    public static boolean delete(String email, int ordinalNum) throws SQLException {
        if (isBlank(email))
            return false;
        email = email.trim();

        Address existing = findOne(email, ordinalNum);
        if (existing == null)
            return false;

        if (!AddressDAO.delete(email, ordinalNum))
            return false;

        if (existing.isDefault()) {
            List<Address> remaining = AddressDAO.findByEmail(email);
            if (remaining != null && !remaining.isEmpty()) {
                if (!AddressDAO.switchDefault(email, remaining.get(0).getOrdinalNum()))
                    throw new SQLException("Đã xóa địa chỉ nhưng không đặt được địa chỉ mặc định mới");
            }
        }
        return true;
    }

    /** Đặt địa chỉ mặc định: bỏ mặc định cũ + đặt mặc định mới trong một transaction. */
    public static boolean setDefault(String email, int ordinalNum) throws SQLException {
        if (isBlank(email))
            return false;
        email = email.trim();

        if (findOne(email, ordinalNum) == null)
            return false; // địa chỉ không tồn tại thì không được bỏ mặc định cũ

        return AddressDAO.switchDefault(email, ordinalNum);
    }

    /** Lấy địa chỉ mặc định của tài khoản (dùng khi đặt hàng). Không có thì trả null. */
    public static Address getDefault(String email) throws SQLException {
        if (isBlank(email))
            return null;
        return AddressDAO.findDefault(email.trim());
    }

    // ===== Helper =====

    /** Tìm 1 địa chỉ theo khóa (email, ordinalNum); không có thì trả null. */
    private static Address findOne(String email, int ordinalNum) throws SQLException {
        if (isBlank(email))
            return null;
        List<Address> list = AddressDAO.findByEmail(email.trim());
        if (list == null)
            return null;
        for (Address a : list) {
            if (a.getOrdinalNum() == ordinalNum)
                return a;
        }
        return null;
    }

    /** Kiểm tra dữ liệu bắt buộc của địa chỉ. */
    private static boolean isValid(Address a) {
        return a != null
                && !isBlank(a.getAccountEmail())
                && !isBlank(a.getStreet())
                && !isBlank(a.getDistrict())
                && !isBlank(a.getCity());
    }

    /** Trim các trường chuỗi trước khi lưu. */
    private static void normalize(Address a) {
        a.setAccountEmail(a.getAccountEmail().trim());
        a.setStreet(a.getStreet().trim());
        a.setDistrict(a.getDistrict().trim());
        a.setCity(a.getCity().trim());
        if (a.getNote() != null)
            a.setNote(a.getNote().trim());
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}