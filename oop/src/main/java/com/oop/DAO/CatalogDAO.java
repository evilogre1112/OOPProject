package com.oop.DAO;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.Brand;
import com.oop.Model.Category;

public class CatalogDAO {

    // ===== Categories =====

    /** Lấy tất cả danh mục (bảng Categories). */
    public static List<Category> findAllCategories() throws SQLException {
        return null;
    }

    /** Thêm danh mục (id UUID do service sinh sẵn). */
    public static boolean insertCategory(Category category) throws SQLException {
        return false;
    }

    /** Sửa tên danh mục theo Id. */
    public static boolean updateCategory(Category category) throws SQLException {
        return false;
    }

    /** Xóa danh mục theo Id (FK NO ACTION: còn nhóm sản phẩm thì sẽ lỗi, nên kiểm tra countGroupsByCategory trước). */
    public static boolean deleteCategory(String categoryId) throws SQLException {
        return false;
    }

    /** Đếm số nhóm sản phẩm đang thuộc danh mục (dùng để chặn xóa). */
    public static int countGroupsByCategory(String categoryId) throws SQLException {
        return 0;
    }

    // ===== Brand =====

    /** Lấy tất cả thương hiệu. */
    public static List<Brand> findAllBrands() throws SQLException {
        return null;
    }

    /** Thêm thương hiệu (id UUID do service sinh sẵn, Logo có thể null). */
    public static boolean insertBrand(Brand brand) throws SQLException {
        return false;
    }

    /** Sửa tên/logo thương hiệu theo Id. */
    public static boolean updateBrand(Brand brand) throws SQLException {
        return false;
    }

    /** Xóa thương hiệu theo Id (FK NO ACTION: còn nhóm sản phẩm thì sẽ lỗi, nên kiểm tra countGroupsByBrand trước). */
    public static boolean deleteBrand(String brandId) throws SQLException {
        return false;
    }

    /** Đếm số nhóm sản phẩm đang thuộc thương hiệu (dùng để chặn xóa). */
    public static int countGroupsByBrand(String brandId) throws SQLException {
        return 0;
    }
}
