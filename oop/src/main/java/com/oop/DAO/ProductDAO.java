package com.oop.DAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.oop.Model.AttributeValue;
import com.oop.Model.GroupProduct;
import com.oop.Model.Image;
import com.oop.Model.Product;

public class ProductDAO {

    // ===== Group_Product =====

    /** Tìm nhóm sản phẩm: JOIN Products để lọc/sắp theo giá (MIN(Price)), LIKE theo tên. Tham số null = bỏ qua. sortBy: "price_asc", "price_desc", "newest". */
    public static List<GroupProduct> searchGroups(String categoryId, String brandId,
            Double minPrice, Double maxPrice, String keyword, String sortBy) throws SQLException {
        return null;
    }

    /** Lấy nhóm sản phẩm theo Id. Không có thì trả null. */
    public static GroupProduct findGroupById(String groupId) throws SQLException {
        return null;
    }

    /** Thêm nhóm sản phẩm (id UUID do service sinh sẵn). */
    public static boolean insertGroup(GroupProduct group) throws SQLException {
        return false;
    }

    /** Sửa nhóm sản phẩm (tên, mô tả, danh mục, thương hiệu). */
    public static boolean updateGroup(GroupProduct group) throws SQLException {
        return false;
    }

    /** Xóa nhóm sản phẩm (Products của nhóm bị xóa theo CASCADE; lỗi nếu biến thể đã có trong đơn hàng). */
    public static boolean deleteGroup(String groupId) throws SQLException {
        return false;
    }

    // ===== Products (biến thể) =====

    /** Lấy các biến thể của một nhóm. */
    public static List<Product> findVariantsByGroup(String groupId) throws SQLException {
        return null;
    }

    /** Lấy một biến thể theo Id. Không có thì trả null. */
    public static Product findVariantById(String productId) throws SQLException {
        return null;
    }

    /** Lấy giá thấp nhất trong các biến thể đang bán (Is_active = 1) của nhóm. */
    public static double findMinPrice(String groupId) throws SQLException {
        return 0;
    }

    /** Thêm biến thể (id UUID do service sinh sẵn). */
    public static boolean insertVariant(Product product) throws SQLException {
        return false;
    }

    /** Sửa biến thể (giá, tồn kho, Is_active). */
    public static boolean updateVariant(Product product) throws SQLException {
        return false;
    }

    /** Xóa biến thể (lỗi FK nếu đã có trong Other_detail; khi đó nên chỉ tắt Is_active). */
    public static boolean deleteVariant(String productId) throws SQLException {
        return false;
    }

    /** Lấy các biến thể có Stock_quantity dưới ngưỡng cảnh báo. */
    public static List<Product> findLowStock(int threshold) throws SQLException {
        return null;
    }

    /** Trừ tồn kho trong transaction: UPDATE ... WHERE Stock_quantity >= quantity. Trả false nếu không đủ hàng. */
    public static boolean decreaseStock(Connection conn, String productId, int quantity) throws SQLException {
        return false;
    }

    /** Cộng lại tồn kho trong transaction (khi hủy đơn). */
    public static boolean increaseStock(Connection conn, String productId, int quantity) throws SQLException {
        return false;
    }

    // ===== Attribute_value + Have =====

    /** Lấy các thuộc tính của một biến thể (JOIN bảng Have). */
    public static List<AttributeValue> findAttributesByProduct(String productId) throws SQLException {
        return null;
    }

    /** Thêm giá trị thuộc tính (id UUID do service sinh sẵn). */
    public static boolean insertAttributeValue(AttributeValue attr) throws SQLException {
        return false;
    }

    /** Sửa giá trị thuộc tính. */
    public static boolean updateAttributeValue(AttributeValue attr) throws SQLException {
        return false;
    }

    /** Xóa giá trị thuộc tính (bảng Have tự xóa theo CASCADE). */
    public static boolean deleteAttributeValue(String attributeValueId) throws SQLException {
        return false;
    }

    /** Gắn thuộc tính vào biến thể (thêm dòng vào bảng Have). */
    public static boolean linkAttribute(String productId, String attributeValueId) throws SQLException {
        return false;
    }

    /** Gỡ thuộc tính khỏi biến thể (xóa dòng trong bảng Have). */
    public static boolean unlinkAttribute(String productId, String attributeValueId) throws SQLException {
        return false;
    }

    // ===== Images =====

    /** Lấy ảnh của một biến thể, sắp theo Ordinal_num. */
    public static List<Image> findImagesByProduct(String productId) throws SQLException {
        return null;
    }

    /** Lấy ảnh của tất cả biến thể trong một nhóm (JOIN Products), sắp theo Ordinal_num. */
    public static List<Image> findImagesByGroup(String groupId) throws SQLException {
        return null;
    }

    /** Thêm ảnh (khóa: Products_Id + Name). */
    public static boolean insertImage(Image image) throws SQLException {
        return false;
    }

    /** Xóa ảnh theo khóa (Products_Id, Name). */
    public static boolean deleteImage(String productId, String name) throws SQLException {
        return false;
    }
}
