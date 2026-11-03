package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.Model.AttributeValue;
import com.oop.Model.GroupProduct;
import com.oop.Model.Image;
import com.oop.Model.Product;

public class ProductDAO {

    // ===== Group_Product =====

    /** Giữ nguyên chữ ký gốc 6 tham số của project, gọi sang hàm phân trang bên dưới. */
    public static List<GroupProduct> searchGroups(String categoryId, String brandId,
                                                  Double minPrice, Double maxPrice,
                                                  String keyword, String sortBy) throws SQLException {
        return searchGroups(categoryId, brandId, minPrice, maxPrice, keyword, sortBy, 0, 0);
    }

    /** Bản mở rộng có hỗ trợ phân trang offset/limit và đẩy NULL xuống cuối khi sort price_asc. */
    public static List<GroupProduct> searchGroups(String categoryId, String brandId,
                                                  Double minPrice, Double maxPrice,
                                                  String keyword, String sortBy,
                                                  int offset, int limit) throws SQLException {
        List<GroupProduct> groups = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT gp.Id, gp.Name, gp.Description, gp.Categories_Id, gp.Brand_Id, MIN(p.Price) AS MinPrice "
                        + "FROM Group_Product gp "
                        + "LEFT JOIN Products p ON gp.Id = p.Group_Product_Id AND p.Is_active = 1 "
                        + "WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (categoryId != null && !categoryId.trim().isEmpty()) {
            query.append("AND gp.Categories_Id = ? ");
            params.add(categoryId.trim());
        }
        if (brandId != null && !brandId.trim().isEmpty()) {
            query.append("AND gp.Brand_Id = ? ");
            params.add(brandId.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append("AND gp.Name LIKE ? ");
            params.add("%" + keyword.trim() + "%");
        }

        query.append("GROUP BY gp.Id, gp.Name, gp.Description, gp.Categories_Id, gp.Brand_Id ");

        boolean hasHaving = false;
        if (minPrice != null) {
            query.append("HAVING MIN(p.Price) >= ? ");
            params.add(minPrice);
            hasHaving = true;
        }
        if (maxPrice != null) {
            query.append(hasHaving ? "AND MIN(p.Price) <= ? " : "HAVING MIN(p.Price) <= ? ");
            params.add(maxPrice);
        }

        if ("price_asc".equalsIgnoreCase(sortBy)) {
            query.append("ORDER BY CASE WHEN MIN(p.Price) IS NULL THEN 1 ELSE 0 END, MinPrice ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            query.append("ORDER BY MinPrice DESC ");
        } else if ("newest".equalsIgnoreCase(sortBy)) {
            query.append("ORDER BY gp.Id DESC ");
        }

        if (limit > 0) {
            query.append("LIMIT ? OFFSET ? ");
            params.add(limit);
            params.add(offset);
        }

        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    GroupProduct group = new GroupProduct();
                    group.setId(resultSet.getString("Id"));
                    group.setName(resultSet.getString("Name"));
                    group.setDescription(resultSet.getString("Description"));
                    group.setCategoryId(resultSet.getString("Categories_Id"));
                    group.setBrandId(resultSet.getString("Brand_Id"));
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    public static GroupProduct findGroupById(String groupId) throws SQLException {
        String query = "SELECT * FROM Group_Product WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    GroupProduct group = new GroupProduct();
                    group.setId(resultSet.getString("Id"));
                    group.setName(resultSet.getString("Name"));
                    group.setDescription(resultSet.getString("Description"));
                    group.setCategoryId(resultSet.getString("Categories_Id"));
                    group.setBrandId(resultSet.getString("Brand_Id"));
                    return group;
                }
            }
        }
        return null;
    }

    public static boolean insertGroup(GroupProduct group) throws SQLException {
        try (Connection databaseConnection = DBContext.getConnection()) {
            return insertGroup(databaseConnection, group);
        }
    }

    /** Overload nhận Connection phục vụ Transaction. */
    public static boolean insertGroup(Connection databaseConnection, GroupProduct group) throws SQLException {
        String queryUpdate = "INSERT INTO Group_Product (Id, Name, Description, Categories_Id, Brand_Id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, group.getId());
            preparedStatement.setString(2, group.getName());
            preparedStatement.setString(3, group.getDescription());
            preparedStatement.setString(4, group.getCategoryId());
            preparedStatement.setString(5, group.getBrandId());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean updateGroup(GroupProduct group) throws SQLException {
        String queryUpdate = "UPDATE Group_Product SET Name = ?, Description = ?, Categories_Id = ?, Brand_Id = ? WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, group.getName());
            preparedStatement.setString(2, group.getDescription());
            preparedStatement.setString(3, group.getCategoryId());
            preparedStatement.setString(4, group.getBrandId());
            preparedStatement.setString(5, group.getId());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean deleteGroup(String groupId) throws SQLException {
        String queryUpdate = "DELETE FROM Group_Product WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, groupId);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    // ===== Products (biến thể) =====

    public static List<Product> findVariantsByGroup(String groupId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Products WHERE Group_Product_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Product product = new Product();
                    product.setId(resultSet.getString("Id"));
                    product.setPrice(resultSet.getDouble("Price"));
                    product.setActive(resultSet.getBoolean("Is_active"));
                    product.setStockQuantity(resultSet.getInt("Stock_quantity"));
                    product.setGroupProductId(resultSet.getString("Group_Product_Id"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    public static Product findVariantById(String productId) throws SQLException {
        String query = "SELECT * FROM Products WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Product product = new Product();
                    product.setId(resultSet.getString("Id"));
                    product.setPrice(resultSet.getDouble("Price"));
                    product.setActive(resultSet.getBoolean("Is_active"));
                    product.setStockQuantity(resultSet.getInt("Stock_quantity"));
                    product.setGroupProductId(resultSet.getString("Group_Product_Id"));
                    return product;
                }
            }
        }
        return null;
    }

    public static double findMinPrice(String groupId) throws SQLException {
        String query = "SELECT COALESCE(MIN(Price), 0) AS MinPrice FROM Products WHERE Group_Product_Id = ? AND Is_active = 1";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("MinPrice");
                }
            }
        }
        return 0;
    }

    public static boolean insertVariant(Product product) throws SQLException {
        try (Connection databaseConnection = DBContext.getConnection()) {
            return insertVariant(databaseConnection, product);
        }
    }

    /** Overload nhận Connection phục vụ Transaction. */
    public static boolean insertVariant(Connection databaseConnection, Product product) throws SQLException {
        String queryUpdate = "INSERT INTO Products (Id, Price, Is_active, Stock_quantity, Group_Product_Id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, product.getId());
            preparedStatement.setDouble(2, product.getPrice());
            preparedStatement.setBoolean(3, product.isActive());
            preparedStatement.setInt(4, product.getStockQuantity());
            preparedStatement.setString(5, product.getGroupProductId());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean updateVariant(Product product) throws SQLException {
        String queryUpdate = "UPDATE Products SET Price = ?, Is_active = ?, Stock_quantity = ? WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setDouble(1, product.getPrice());
            preparedStatement.setBoolean(2, product.isActive());
            preparedStatement.setInt(3, product.getStockQuantity());
            preparedStatement.setString(4, product.getId());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean deleteVariant(String productId) throws SQLException {
        String queryUpdate = "DELETE FROM Products WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, productId);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static List<Product> findLowStock(int threshold) throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM Products WHERE Stock_quantity < ? ORDER BY Stock_quantity ASC";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setInt(1, threshold);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Product product = new Product();
                    product.setId(resultSet.getString("Id"));
                    product.setPrice(resultSet.getDouble("Price"));
                    product.setActive(resultSet.getBoolean("Is_active"));
                    product.setStockQuantity(resultSet.getInt("Stock_quantity"));
                    product.setGroupProductId(resultSet.getString("Group_Product_Id"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    public static boolean decreaseStock(Connection databaseConnection, String productId, int quantity) throws SQLException {
        String queryUpdate = "UPDATE Products SET Stock_quantity = Stock_quantity - ? WHERE Id = ? AND Stock_quantity >= ? AND Is_active = 1";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setString(2, productId);
            preparedStatement.setInt(3, quantity);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean increaseStock(Connection databaseConnection, String productId, int quantity) throws SQLException {
        String queryUpdate = "UPDATE Products SET Stock_quantity = Stock_quantity + ? WHERE Id = ?";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setString(2, productId);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    // ===== Attribute_value + Have =====

    public static List<AttributeValue> findAttributesByProduct(String productId) throws SQLException {
        List<AttributeValue> attributes = new ArrayList<>();
        String query = "SELECT av.Id, av.Name, av.Value "
                + "FROM Attribute_value av "
                + "INNER JOIN Have h ON av.Id = h.Attribute_value_Id "
                + "WHERE h.Products_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    AttributeValue attr = new AttributeValue();
                    attr.setId(resultSet.getString("Id"));
                    attr.setName(resultSet.getString("Name"));
                    attr.setValue(resultSet.getString("Value"));
                    attributes.add(attr);
                }
            }
        }
        return attributes;
    }

    public static boolean insertAttributeValue(AttributeValue attr) throws SQLException {
        String queryUpdate = "INSERT INTO Attribute_value (Id, Name, Value) VALUES (?, ?, ?)";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, attr.getId());
            preparedStatement.setString(2, attr.getName());
            preparedStatement.setString(3, attr.getValue());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean updateAttributeValue(AttributeValue attr) throws SQLException {
        String queryUpdate = "UPDATE Attribute_value SET Name = ?, Value = ? WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, attr.getName());
            preparedStatement.setString(2, attr.getValue());
            preparedStatement.setString(3, attr.getId());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean deleteAttributeValue(String attributeValueId) throws SQLException {
        String queryUpdate = "DELETE FROM Attribute_value WHERE Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, attributeValueId);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean linkAttribute(String productId, String attributeValueId) throws SQLException {
        try (Connection databaseConnection = DBContext.getConnection()) {
            return linkAttribute(databaseConnection, productId, attributeValueId);
        }
    }

    /** Overload liên kết thuộc tính trong Transaction. */
    public static boolean linkAttribute(Connection databaseConnection, String productId, String attributeValueId) throws SQLException {
        String queryUpdate = "INSERT INTO Have (Attribute_value_Id, Products_Id) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, attributeValueId);
            preparedStatement.setString(2, productId);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean unlinkAttribute(String productId, String attributeValueId) throws SQLException {
        String queryUpdate = "DELETE FROM Have WHERE Attribute_value_Id = ? AND Products_Id = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, attributeValueId);
            preparedStatement.setString(2, productId);
            return preparedStatement.executeUpdate() == 1;
        }
    }

    // ===== Images =====

    public static List<Image> findImagesByProduct(String productId) throws SQLException {
        List<Image> images = new ArrayList<>();
        String query = "SELECT * FROM Images WHERE Products_Id = ? ORDER BY Ordinal_num ASC";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Image img = new Image();
                    img.setName(resultSet.getString("Name"));
                    img.setUrl(resultSet.getString("url"));
                    img.setOrdinalNum(resultSet.getInt("Ordinal_num"));
                    img.setProductId(resultSet.getString("Products_Id"));
                    images.add(img);
                }
            }
        }
        return images;
    }

    public static List<Image> findImagesByGroup(String groupId) throws SQLException {
        List<Image> images = new ArrayList<>();
        String query = "SELECT i.Name, i.url, i.Ordinal_num, i.Products_Id "
                + "FROM Images i "
                + "INNER JOIN Products p ON i.Products_Id = p.Id "
                + "WHERE p.Group_Product_Id = ? "
                + "ORDER BY i.Ordinal_num ASC";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setString(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Image img = new Image();
                    img.setName(resultSet.getString("Name"));
                    img.setUrl(resultSet.getString("url"));
                    img.setOrdinalNum(resultSet.getInt("Ordinal_num"));
                    img.setProductId(resultSet.getString("Products_Id"));
                    images.add(img);
                }
            }
        }
        return images;
    }

    public static boolean insertImage(Image image) throws SQLException {
        try (Connection databaseConnection = DBContext.getConnection()) {
            return insertImage(databaseConnection, image);
        }
    }

    /** Overload thêm ảnh trong Transaction. */
    public static boolean insertImage(Connection databaseConnection, Image image) throws SQLException {
        String queryUpdate = "INSERT INTO Images (Name, url, Ordinal_num, Products_Id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, image.getName());
            preparedStatement.setString(2, image.getUrl());
            preparedStatement.setInt(3, image.getOrdinalNum());
            preparedStatement.setString(4, image.getProductId());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    /** Cập nhật ảnh (đổi đường dẫn URL hoặc sắp xếp lại thứ tự Ordinal_num). */
    public static boolean updateImage(Image image) throws SQLException {
        String queryUpdate = "UPDATE Images SET url = ?, Ordinal_num = ? WHERE Products_Id = ? AND Name = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, image.getUrl());
            preparedStatement.setInt(2, image.getOrdinalNum());
            preparedStatement.setString(3, image.getProductId());
            preparedStatement.setString(4, image.getName());
            return preparedStatement.executeUpdate() == 1;
        }
    }

    public static boolean deleteImage(String productId, String name) throws SQLException {
        String queryUpdate = "DELETE FROM Images WHERE Products_Id = ? AND Name = ?";
        try (Connection databaseConnection = DBContext.getConnection();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(queryUpdate)) {
            preparedStatement.setString(1, productId);
            preparedStatement.setString(2, name);
            return preparedStatement.executeUpdate() == 1;
        }
    }
}