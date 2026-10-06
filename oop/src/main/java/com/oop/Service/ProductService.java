package com.oop.Service;

import java.sql.SQLException;
import java.util.List;

import com.oop.Model.AttributeValue;
import com.oop.Model.GroupProduct;
import com.oop.Model.Image;
import com.oop.Model.Product;

public class ProductService {

    // ===== Xem & tìm kiếm (Customer) =====

    /** Lọc nhóm sản phẩm theo danh mục, thương hiệu, khoảng giá, từ khóa tên. Tham số null = bỏ qua. sortBy: "price_asc", "price_desc", "newest". */
    public static List<GroupProduct> search(String categoryId, String brandId,
            Double minPrice, Double maxPrice, String keyword, String sortBy) throws SQLException {
        return null;
    }

    /** Lấy một nhóm sản phẩm theo id (mô tả, danh mục, thương hiệu). */
    public static GroupProduct getGroupById(String groupId) throws SQLException {
        return null;
    }

    /** Lấy các biến thể của nhóm (kèm giá, tồn kho). */
    public static List<Product> getVariants(String groupId) throws SQLException {
        return null;
    }

    /** Lấy giá thấp nhất trong các biến thể đang bán của nhóm (hiển thị ở danh sách). */
    public static double getMinPrice(String groupId) throws SQLException {
        return 0;
    }

    /** Lấy danh sách ảnh của nhóm sản phẩm, sắp theo ordinalNum. */
    public static List<Image> getImages(String groupId) throws SQLException {
        return null;
    }

    /** Lấy các thuộc tính (size, màu...) của một biến thể. */
    public static List<AttributeValue> getAttributes(String productId) throws SQLException {
        return null;
    }

    // ===== CRUD nhóm sản phẩm (Admin) =====

    /** [Admin] Thêm nhóm sản phẩm + ghi nhật ký. */
    public static boolean addGroup(GroupProduct group) throws SQLException {
        return false;
    }

    /** [Admin] Sửa nhóm sản phẩm + ghi nhật ký. */
    public static boolean updateGroup(GroupProduct group) throws SQLException {
        return false;
    }

    /** [Admin] Xóa nhóm sản phẩm + ghi nhật ký. */
    public static boolean deleteGroup(String groupId) throws SQLException {
        return false;
    }

    // ===== CRUD biến thể (Admin) =====

    /** [Admin] Thêm biến thể (giá, tồn kho, isActive) + ghi nhật ký. */
    public static boolean addVariant(Product product) throws SQLException {
        return false;
    }

    /** [Admin] Sửa biến thể (kể cả bật/tắt isActive) + ghi nhật ký. */
    public static boolean updateVariant(Product product) throws SQLException {
        return false;
    }

    /** [Admin] Xóa biến thể + ghi nhật ký. */
    public static boolean deleteVariant(String productId) throws SQLException {
        return false;
    }

    /** [Admin] Lấy các biến thể có tồn kho dưới ngưỡng cảnh báo. */
    public static List<Product> getLowStock(int threshold) throws SQLException {
        return null;
    }

    // ===== Thuộc tính & ảnh (Admin) =====

    /** [Admin] Thêm giá trị thuộc tính mới (vd: Size = M). */
    public static boolean addAttributeValue(AttributeValue attr) throws SQLException {
        return false;
    }

    /** [Admin] Sửa giá trị thuộc tính. */
    public static boolean updateAttributeValue(AttributeValue attr) throws SQLException {
        return false;
    }

    /** [Admin] Xóa giá trị thuộc tính (xóa luôn liên kết ProductAttribute). */
    public static boolean deleteAttributeValue(String attributeValueId) throws SQLException {
        return false;
    }

    /** [Admin] Gắn thuộc tính vào biến thể (thêm ProductAttribute). */
    public static boolean linkAttribute(String productId, String attributeValueId) throws SQLException {
        return false;
    }

    /** [Admin] Gỡ thuộc tính khỏi biến thể. */
    public static boolean unlinkAttribute(String productId, String attributeValueId) throws SQLException {
        return false;
    }

    /** [Admin] Thêm ảnh cho sản phẩm. */
    public static boolean addImage(Image image) throws SQLException {
        return false;
    }

    /** [Admin] Xóa ảnh theo khóa (productId, name). */
    public static boolean deleteImage(String productId, String name) throws SQLException {
        return false;
    }
}
