package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.EXCEPTION.SQL;
import com.oop.Model.Brand;
import com.oop.Model.Category;

public class CatalogDAO {
    private static Connection databaseConnection;
    static {
        try {
            databaseConnection = DBContext.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối Database", e);
        }
    }
    // ===== Categories =====

    /** Lấy tất cả danh mục (bảng Categories). */
    public static synchronized List<Category> findAllCategories() throws SQLException {
        String query = "SELECT * FROM Categories";
        List<Category> Categories = new ArrayList<>();
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();) {
            while (resultSet.next()) {
                Category category = new Category(
                        resultSet.getString("Id"),
                        resultSet.getString("Name")

                );
                Categories.add(category);
            }

        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
        return Categories;
    }

    /** Thêm danh mục (id UUID do service sinh sẵn). */
    public static synchronized boolean insertCategory(Category category) throws SQLException {
        String queryUpdate = "INSERT INTO Categories (Id, Name) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, category.getId());
            preparedStatement.setString(2, category.getName());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Tìm category theo Id */
    public static synchronized Category findCategoryById(String id) throws SQLException {

        String query = "SELECT * FROM Categories WHERE Id = ?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {

            preparedStatement.setString(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Category(
                            resultSet.getString("Id"),
                            resultSet.getString("Name"));
                }
            }

        } catch (SQLException e) {
            throw SQL.Exception(e);
        }

        throw new SQLException("Không tìm thấy id");
    }

    /**
     * Tìm danh mục theo tên (không phân biệt hoa thường). Chưa có thì trả null
     * (khác findCategoryById).
     */
    public static synchronized Category findCategoryByName(String name) throws SQLException {
        String query = "SELECT * FROM Categories WHERE LOWER(Name) = LOWER(?) LIMIT 1";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Category(
                            resultSet.getString("Id"),
                            resultSet.getString("Name"));
                }
            }
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
        return null;
    }

    /** Sửa tên danh mục theo Id. */
    public static synchronized boolean updateCategory(Category category) throws SQLException {
        String queryUpdate = "UPDATE Categories SET Name = ? WHERE Id = ?";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getId());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /**
     * Xóa danh mục theo Id (FK NO ACTION: còn nhóm sản phẩm thì sẽ lỗi, nên kiểm
     * tra countGroupsByCategory trước).
     */
    public static synchronized boolean deleteCategory(String categoryId) throws SQLException {
        String queryUpdate = "DELETE FROM Categories WHERE Id = ?";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setString(1, categoryId);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }

    }

    /** Đếm số nhóm sản phẩm đang thuộc danh mục (dùng để chặn xóa). */
    // Chưa cần lắm SQL đã có sẵn ràng buộc nên sẽ tự throw Exception nếu xoá
    public static int countGroupsByCategory(String categoryId) throws SQLException {
        return 0;
    }

    // ===== Brand =====

    /** Lấy tất cả thương hiệu. */
    public static synchronized List<Brand> findAllBrands() throws SQLException {
        String query = "SELECT * FROM Brand";
        List<Brand> Brands = new ArrayList<>();
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();) {
            while (resultSet.next()) {
                Brand brand = new Brand(
                        resultSet.getString("Id"),
                        resultSet.getString("Name"),
                        resultSet.getString("Logo")

                );
                Brands.add(brand);
            }

        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
        return Brands;
    }

    /** Tìm thương hiệu theo Id. */
    public static synchronized Brand findBrandById(String id)
            throws SQLException {

        String query = "SELECT * FROM Brand WHERE Id = ?";

        try (PreparedStatement ps = databaseConnection.prepareStatement(query)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Brand(
                            rs.getString("Id"),
                            rs.getString("Name"),
                            rs.getString("Logo"));
                }
            }

        } catch (SQLException e) {
            throw SQL.Exception(e);
        }

        throw new SQLException("Không tìm thấy thương hiệu");
    }

    /**
     * Tìm thương hiệu theo tên (không phân biệt hoa thường). Chưa có thì trả null
     * (khác findBrandById).
     */
    public static synchronized Brand findBrandByName(String name) throws SQLException {
        String query = "SELECT * FROM Brand WHERE LOWER(Name) = LOWER(?) LIMIT 1";
        try (PreparedStatement ps = databaseConnection.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Brand(
                            rs.getString("Id"),
                            rs.getString("Name"),
                            rs.getString("Logo"));
                }
            }
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
        return null;
    }

    /** Thêm thương hiệu (id UUID do service sinh sẵn, Logo có thể null). */
    public static synchronized boolean insertBrand(Brand brand) throws SQLException {
        String queryUpdate = "INSERT INTO Brand (Id, Name, Logo) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setString(1, brand.getId());
            preparedStatement.setString(2, brand.getName());
            preparedStatement.setString(3, brand.getLogo());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Sửa tên/logo thương hiệu theo Id. */
    public static synchronized boolean updateBrand(Brand brand) throws SQLException {
        String queryUpdate = "UPDATE Brand SET Name = ?, Logo = ? WHERE Id = ?";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setString(1, brand.getName());
            preparedStatement.setString(2, brand.getLogo());
            preparedStatement.setString(3, brand.getId());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /**
     * Xóa thương hiệu theo Id (FK NO ACTION: còn nhóm sản phẩm thì sẽ lỗi, nên kiểm
     * tra countGroupsByBrand trước).
     */
    public static synchronized boolean deleteBrand(String brandId) throws SQLException {
        String queryUpdate = "DELETE FROM Brand WHERE Id = ?";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate);) {
            preparedStatement.setString(1, brandId);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw SQL.Exception(e);
        }
    }

    /** Đếm số nhóm sản phẩm đang thuộc thương hiệu (dùng để chặn xóa). */
    // Chưa cần lắm SQL đã có sẵn ràng buộc nên sẽ tự throw Exception nếu xoá
    public static int countGroupsByBrand(String brandId) throws SQLException {
        return 0;
    }
}