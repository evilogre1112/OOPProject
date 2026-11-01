package com.oop.Service;

import java.sql.SQLException;
import java.util.List;

import com.oop.DAO.AddressDAO;
import com.oop.Model.Address;

public class AddressService {

    /** Lấy sổ địa chỉ của một tài khoản. */
    public static List<Address> getByEmail(String email) throws SQLException {
        if (isBlank(email))
            return List.of();
        return AddressDAO.findByEmail(email);
    }

    /** Thêm địa chỉ: ordinalNum tự tăng theo tài khoản; địa chỉ đầu tiên tự là mặc định. */
    public static boolean add(Address address) throws SQLException {
        if (!isValid(address))
            return false;

        String email = address.getAccountEmail();
        boolean wantDefault = address.isDefault();

        // Gán số thứ tự mới (MAX + 1, bắt đầu từ 1)
        int ordinal = AddressDAO.getNextOrdinal(email);
        address.setOrdinalNum(ordinal);

        // Địa chỉ đầu tiên của tài khoản luôn là mặc định
        boolean isFirst = (ordinal == 1);
        // Chèn trước với cờ mặc định đúng nếu là địa chỉ đầu, còn lại tạm để false
        address.setDefault(isFirst);

        if (!AddressDAO.insert(address))
            return false;

        // Nếu người dùng muốn địa chỉ mới này là mặc định (và không phải địa chỉ đầu)
        if (wantDefault && !isFirst)
            return setDefault(email, ordinal);

        return true;
    }

    /** Sửa địa chỉ theo khóa (accountEmail, ordinalNum). */
    public static boolean update(Address address) throws SQLException {
        if (!isValid(address))
            return false;

        Address existing = findOne(address.getAccountEmail(), address.getOrdinalNum());
        if (existing == null)
            return false;

        // Không đổi cờ mặc định ở đây, việc đó phải đi qua setDefault()
        // để luôn đảm bảo mỗi tài khoản chỉ có 1 địa chỉ mặc định.
        address.setDefault(existing.isDefault());
        return AddressDAO.update(address);
    }

    /** Xóa địa chỉ theo khóa (accountEmail, ordinalNum). */
    public static boolean delete(String email, int ordinalNum) throws SQLException {
        Address existing = findOne(email, ordinalNum);
        if (existing == null)
            return false;

        if (!AddressDAO.delete(email, ordinalNum))
            return false;

        // Nếu vừa xóa địa chỉ mặc định mà vẫn còn địa chỉ khác -> chọn địa chỉ đầu tiên làm mặc định
        if (existing.isDefault()) {
            List<Address> remaining = AddressDAO.findByEmail(email);
            if (remaining != null && !remaining.isEmpty())
                AddressDAO.setDefault(email, remaining.get(0).getOrdinalNum());
        }
        return true;
    }

    /** Đặt địa chỉ mặc định: bỏ mặc định cũ, đặt mặc định mới (đảm bảo chỉ có 1). */
    public static boolean setDefault(String email, int ordinalNum) throws SQLException {
        if (findOne(email, ordinalNum) == null)
            return false; // địa chỉ không tồn tại thì không được bỏ mặc định cũ

        AddressDAO.clearDefault(email); // không check kết quả: có thể chưa có dòng nào là mặc định
        return AddressDAO.setDefault(email, ordinalNum);
    }

    /** Lấy địa chỉ mặc định của tài khoản (dùng khi đặt hàng). */
    public static Address getDefault(String email) throws SQLException {
        if (isBlank(email))
            return null;
        return AddressDAO.findDefault(email);
    }

    // ===== Helper =====

    /** Tìm 1 địa chỉ theo khóa (email, ordinalNum); không có thì trả null. */
    private static Address findOne(String email, int ordinalNum) throws SQLException {
        if (isBlank(email))
            return null;
        List<Address> list = AddressDAO.findByEmail(email);
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

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}