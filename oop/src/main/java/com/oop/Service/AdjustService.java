package com.oop.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import com.oop.DAO.AdjustDAO;
import com.oop.EXCEPTION.SQL;
import com.oop.Model.Account;
import com.oop.Model.Adjust;

public class AdjustService {
    /** Ghi nhật ký cho Admin đang đăng nhập (lấy SĐT từ AccountService.getCurrentAccount()). typeAdjust: "Thêm"/"Sửa"/"Xóa"; typeObject: "Sản phẩm"/"Danh mục"...; createdAt lấy giờ hiện tại. 
     * 
     * Sử dụng sau khi Admin thực hiện thành công
     * một thao tác thay đổi dữ liệu.
     *
     * @param accountService AccountService đã đăng nhập.
     *                       Truyền cùng đối tượng đang quản lý
     *                       tài khoản Admin hiện tại.
     *
     * @param typeAdjust Loại thao tác:
     *                   "Thêm", "Sửa", "Xóa".
     *
     * @param objectName Tên đối tượng bị thay đổi.
     *                   Ví dụ: "Áo thun nam", "Giày thể thao".
     *
     * @param typeObject Loại đối tượng bị thay đổi.
     *                   Ví dụ: "Sản phẩm", "Danh mục",
     *                   "Thương hiệu", "Tài khoản".
     *
     * @param objectId ID của đối tượng bị thay đổi.
     *                 Phải truyền ID thật từ database.
     *                 Kiểu String, tối đa 36 ký tự.
     *
     * @throws SQLException Nếu chưa đăng nhập,
     *                      không có quyền Admin
     *                      hoặc ghi database thất bại.
    */
    public static void log(AccountService accountService, String typeAdjust, String objectName, String typeObject, String objectId) throws SQLException {
        Account account = requireAdmin(accountService);

        if (account == null) {
            throw new SQLException("Chưa đăng nhập");
        }

        if (!"Admin".equals(account.getRole())) {
            throw new SQLException(
                "Chỉ Admin mới được ghi nhật ký điều chỉnh"
            );
        }

        if (typeAdjust == null || typeAdjust.isBlank()
                || objectName == null || objectName.isBlank()
                || typeObject == null || typeObject.isBlank()
                || objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException(
                "Thông tin nhật ký không được để trống"
            );
        }

        Adjust adjust = new Adjust();

        adjust.setAdminPhoneNum(account.getPhoneNum());
        adjust.setTypeAdjust(typeAdjust);
        adjust.setObjectNameAdjusted(objectName);
        adjust.setTypeObject(typeObject);
        adjust.setIdObjectAdjusted(objectId);

        try {
            if (!AdjustDAO.insert(adjust)) {
                throw new SQLException(
                    "Không thể ghi nhật ký điều chỉnh"
                );
            }
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** [Admin] Lấy toàn bộ nhật ký, mới nhất trước. */
    public static List<Adjust> getAll(AccountService accountService) throws SQLException {
         requireAdmin(accountService);

        try {
            return AdjustDAO.findAll();
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** [Admin] Lọc nhật ký theo SĐT admin và khoảng ngày. Tham số null = bỏ qua. */
    public static List<Adjust> filter(AccountService accountService, String adminPhone, LocalDate from, LocalDate to) throws SQLException {
         requireAdmin(accountService);

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                "Ngày bắt đầu không được sau ngày kết thúc"
            );
        }

        try {
            return AdjustDAO.filter(adminPhone, from, to);
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }
     /**
     * Kiểm tra tài khoản hiện tại có quyền Admin hay không.
     *
     * @param accountService Đối tượng AccountService đã dùng
     *                       để đăng nhập bằng login().
     *                       KHÔNG tạo AccountService mới ở đây.
     *
     * @return Account của Admin đang đăng nhập.
     *
     * @throws SQLException Nếu chưa đăng nhập hoặc không phải Admin.
     *
     * Chỉ sử dụng nội bộ trong AdjustService.
     */
    private static Account requireAdmin(AccountService accountService)
            throws SQLException {

        if (accountService == null) {
            throw new SQLException("Chưa có phiên đăng nhập");
        }

        Account account = accountService.getCurrentAccount();

        if (account == null) {
            throw new SQLException("Chưa đăng nhập");
        }

        if (!"Admin".equals(account.getRole())) {
            throw new SQLException("Không có quyền Admin");
        }

        return account;
    }

}
