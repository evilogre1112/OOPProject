package com.oop.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.oop.DAO.AdjustDAO;
import com.oop.DAO.CatalogDAO;
import com.oop.EXCEPTION.PermissionException;
import com.oop.Model.Account;
import com.oop.Model.Adjust;
import com.oop.Model.Brand;
import com.oop.Model.Category;

public class CatalogService {

    // ===== Category =====

    /** Lấy tất cả danh mục. */
    public static List<Category> getAllCategories() throws SQLException {
        return CatalogDAO.findAllCategories();
    }

    /** Tìm danh mục theo tên (không phân biệt hoa thường). Chưa có thì trả null. */
    public static Category findCategoryByName(String name) throws SQLException {
        if (isBlank(name))
            return null;
        return CatalogDAO.findCategoryByName(clean(name));
    }

    /**
     * [Admin] Thêm danh mục + ghi nhật ký. Trùng tên danh mục đã có thì báo lỗi.
     */
    public static boolean addCategory(Category category, AccountService service)
            throws SQLException, PermissionException {
        Account user = requireAdmin(service);
        if (category == null || isBlank(category.getName()))
            return false;

        String name = clean(category.getName());
        if (CatalogDAO.findCategoryByName(name) != null)
            throw new SQLException("Danh mục \"" + name + "\" đã tồn tại");

        String id = UUID.randomUUID().toString();
        if (!CatalogDAO.insertCategory(new Category(id, name)))
            return false;
        category.setId(id); // trả id về cho nơi gọi

        return writeLog(user, "INSERT", name, "Category", id); // Phần này thảo luận sau về roll back -.+ quá oải
    }

    /**
     * [Admin] Dùng danh mục theo tên: đã có thì lấy lại danh mục đó, chưa có thì
     * thêm mới rồi trả về danh mục vừa thêm.
     */
    public static Category useCategory(String name, AccountService service) throws SQLException, PermissionException {
        requireAdmin(service);
        if (isBlank(name))
            throw new SQLException("Tên danh mục không được để trống");

        Category existing = CatalogDAO.findCategoryByName(clean(name));
        if (existing != null)
            return existing;

        Category created = new Category(null, clean(name));
        if (!addCategory(created, service))
            throw new SQLException("Không thể thêm danh mục \"" + clean(name) + "\"");
        return created;
    }

    /**
     * [Admin] Sửa danh mục + ghi nhật ký. Đổi sang tên của danh mục khác thì báo
     * lỗi.
     */
    public static boolean updateCategory(Category category, String newName, AccountService service)
            throws SQLException, PermissionException {
        Account user = requireAdmin(service);
        if (category == null || isBlank(newName))
            return false;

        String name = clean(newName);
        Category same = CatalogDAO.findCategoryByName(name);
        if (same != null && !same.getId().equals(category.getId()))
            throw new SQLException("Danh mục \"" + name + "\" đã tồn tại");

        if (!CatalogDAO.updateCategory(new Category(category.getId(), name)))
            return false;

        return writeLog(user, "CHANGENAME", category.getName(), "Category", category.getId()); // Phần này thảo luận sau
                                                                                               // về roll back -.+ quá
                                                                                               // oải
    }

    /** [Admin] Xóa danh mục (chặn nếu còn nhóm sản phẩm liên kết) + ghi nhật ký. */
    public static boolean deleteCategory(String categoryId, AccountService service)
            throws SQLException, PermissionException {
        Account user = requireAdmin(service);
        String Name = CatalogDAO.findCategoryById(categoryId).getName();
        if (!CatalogDAO.deleteCategory(categoryId))
            return false;

        return writeLog(user, "DELETE", Name, "Category", categoryId); // Phần này thảo luận sau về roll back -.+ quá
                                                                       // oải
    }

    // ===== Brand =====

    /** Lấy tất cả thương hiệu. */
    public static List<Brand> getAllBrands() throws SQLException {
        return CatalogDAO.findAllBrands();
    }

    /**
     * Tìm thương hiệu theo tên (không phân biệt hoa thường). Chưa có thì trả null.
     */
    public static Brand findBrandByName(String name) throws SQLException {
        if (isBlank(name))
            return null;
        return CatalogDAO.findBrandByName(clean(name));
    }

    /**
     * [Admin] Thêm thương hiệu (có logo) + ghi nhật ký. Trùng tên thương hiệu đã có
     * thì báo lỗi.
     */
    public static boolean addBrand(Brand brand, AccountService service) throws SQLException, PermissionException {
        Account user = requireAdmin(service);
        if (brand == null || isBlank(brand.getName()))
            return false;

        String name = clean(brand.getName());
        if (CatalogDAO.findBrandByName(name) != null)
            throw new SQLException("Thương hiệu \"" + name + "\" đã tồn tại");

        String id = UUID.randomUUID().toString();
        if (!CatalogDAO.insertBrand(new Brand(id, name, brand.getLogo())))
            return false;
        brand.setId(id); // trả id về cho nơi gọi

        return writeLog(user, "INSERT", name, "Brand", id); // Phần này thảo luận sau về roll back -.+ quá oải
    }

    /**
     * [Admin] Dùng thương hiệu theo tên: đã có thì lấy lại thương hiệu đó (giữ
     * nguyên logo cũ),
     * chưa có thì thêm mới (kèm logo, có thể null) rồi trả về thương hiệu vừa thêm.
     */
    public static Brand useBrand(String name, String logo, AccountService service)
            throws SQLException, PermissionException {
        requireAdmin(service);
        if (isBlank(name))
            throw new SQLException("Tên thương hiệu không được để trống");

        Brand existing = CatalogDAO.findBrandByName(clean(name));
        if (existing != null)
            return existing;

        Brand created = new Brand(null, clean(name), logo);
        if (!addBrand(created, service))
            throw new SQLException("Không thể thêm thương hiệu \"" + clean(name) + "\"");
        return created;
    }

    /**
     * [Admin] Sửa thương hiệu + ghi nhật ký. Đổi sang tên của thương hiệu khác thì
     * báo lỗi.
     */
    public static boolean updateBrand(Brand brand, String newName, String newLogo, AccountService service)
            throws SQLException, PermissionException {
        Account user = requireAdmin(service);
        if (brand == null || isBlank(newName))
            return false;

        String name = clean(newName);
        Brand same = CatalogDAO.findBrandByName(name);
        if (same != null && !same.getId().equals(brand.getId()))
            throw new SQLException("Thương hiệu \"" + name + "\" đã tồn tại");

        if (!CatalogDAO.updateBrand(new Brand(brand.getId(), name, newLogo)))
            return false;

        return writeLog(user, "UPDATE", brand.getName(), "Brand", brand.getId()); // Phần này thảo luận sau về roll back
                                                                                  // -.+ quá oải
    }

    /**
     * [Admin] Xóa thương hiệu (chặn nếu còn nhóm sản phẩm liên kết) + ghi nhật ký.
     */
    public static boolean deleteBrand(String brandId, AccountService service) throws SQLException, PermissionException {
        Account user = requireAdmin(service);
        String Name = CatalogDAO.findBrandById(brandId).getName();
        if (!CatalogDAO.deleteBrand(brandId))
            return false;

        return writeLog(user, "DELETE", Name, "Brand", brandId); // Phần này thảo luận sau về roll back -.+ quá oải
    }

    // ===== Helpers =====

    /**
     * Trả về Admin đang đăng nhập, không phải Admin thì ném PermissionException.
     */
    private static Account requireAdmin(AccountService service) throws PermissionException {
        Account user = service == null ? null : service.getCurrentAccount();
        if (user == null || !"Admin".equals(user.getRole()))
            throw new PermissionException();
        return user;
    }

    /** Ghi một dòng nhật ký điều chỉnh. */
    private static boolean writeLog(Account user, String typeAdjust, String objectName, String typeObject,
            String objectId) throws SQLException {
        Adjust adjust = new Adjust();
        adjust.setAdminPhoneNum(user.getPhoneNum());
        adjust.setTypeAdjust(typeAdjust);
        adjust.setObjectNameAdjusted(objectName);
        adjust.setTypeObject(typeObject);
        adjust.setIdObjectAdjusted(objectId);
        return AdjustDAO.insert(adjust);
    }

    /**
     * Bỏ khoảng trắng đầu cuối và gộp nhiều khoảng trắng liền nhau thành một ("Áo
     * thun " -> "Áo thun").
     */
    private static String clean(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}