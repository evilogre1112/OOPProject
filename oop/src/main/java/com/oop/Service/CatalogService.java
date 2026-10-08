package com.oop.Service;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Brand;
import com.oop.Model.Category;

public class CatalogService {

    // ===== Category =====

    /** Lấy tất cả danh mục. */
    public static List<Category> getAllCategories() throws SQLException {
        return null;
    }

    /** [Admin] Thêm danh mục + ghi nhật ký. */
    public static boolean addCategory(Category category) throws SQLException {
        return false;
    }

    /** [Admin] Sửa danh mục + ghi nhật ký. */
    public static boolean updateCategory(Category category) throws SQLException {
        return false;
    }

    /** [Admin] Xóa danh mục (chặn nếu còn nhóm sản phẩm liên kết) + ghi nhật ký. */
    public static boolean deleteCategory(String categoryId) throws SQLException {
        return false;
    }

    // ===== Brand =====

    /** Lấy tất cả thương hiệu. */
    public static List<Brand> getAllBrands() throws SQLException {
        return null;
    }

    /** [Admin] Thêm thương hiệu (có logo) + ghi nhật ký. */
    public static boolean addBrand(Brand brand) throws SQLException {
        return false;
    }

    /** [Admin] Sửa thương hiệu + ghi nhật ký. */
    public static boolean updateBrand(Brand brand) throws SQLException {
        return false;
    }

    /** [Admin] Xóa thương hiệu (chặn nếu còn nhóm sản phẩm liên kết) + ghi nhật ký. */
    public static boolean deleteBrand(String brandId) throws SQLException {
        return false;
    }
}
