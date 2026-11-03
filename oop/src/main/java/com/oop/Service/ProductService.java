package com.oop.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.oop.DAO.ProductDAO;
import com.oop.Model.AttributeValue;
import com.oop.Model.GroupProduct;
import com.oop.Model.Image;
import com.oop.Model.Product;

public class ProductService {

    // ===== Xem & tìm kiếm (Customer) =====

    /**
     * Lọc nhóm sản phẩm theo danh mục, thương hiệu, khoảng giá, từ khóa tên.
     * Tham số null = bỏ qua. sortBy: "price_asc", "price_desc", "newest".
     */
    public static List<GroupProduct> search(String categoryId, String brandId,
            Double minPrice, Double maxPrice, String keyword, String sortBy) throws SQLException {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return new ArrayList<>();
        }
        return ProductDAO.searchGroups(categoryId, brandId, minPrice, maxPrice, keyword, sortBy);
    }

    /**
     * Bản mở rộng hỗ trợ phân trang cho tìm kiếm nhóm sản phẩm.
     */
    public static List<GroupProduct> search(String categoryId, String brandId,
            Double minPrice, Double maxPrice, String keyword, String sortBy,
            int offset, int limit) throws SQLException {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            return new ArrayList<>();
        }
        return ProductDAO.searchGroups(categoryId, brandId, minPrice, maxPrice, keyword, sortBy, offset, limit);
    }

    /**
     * Lấy một nhóm sản phẩm theo id (mô tả, danh mục, thương hiệu).
     */
    public static GroupProduct getGroupById(String groupId) throws SQLException {
        if (isNullOrEmpty(groupId)) {
            return null;
        }
        return ProductDAO.findGroupById(groupId.trim());
    }

    /**
     * Lấy các biến thể của nhóm (kèm giá, tồn kho).
     */
    public static List<Product> getVariants(String groupId) throws SQLException {
        if (isNullOrEmpty(groupId)) {
            return new ArrayList<>();
        }
        return ProductDAO.findVariantsByGroup(groupId.trim());
    }

    /**
     * Lấy một biến thể theo id.
     */
    public static Product getVariantById(String productId) throws SQLException {
        if (isNullOrEmpty(productId)) {
            return null;
        }
        return ProductDAO.findVariantById(productId.trim());
    }

    /**
     * Lấy giá thấp nhất trong các biến thể đang bán của nhóm (hiển thị ở danh
     * sách).
     */
    public static double getMinPrice(String groupId) throws SQLException {
        if (isNullOrEmpty(groupId)) {
            return 0.0;
        }
        return ProductDAO.findMinPrice(groupId.trim());
    }

    /**
     * Lấy danh sách ảnh của nhóm sản phẩm (tổng hợp các ảnh của các biến thể), sắp
     * theo ordinalNum.
     */
    public static List<Image> getImages(String groupId) throws SQLException {
        if (isNullOrEmpty(groupId)) {
            return new ArrayList<>();
        }
        return ProductDAO.findImagesByGroup(groupId.trim());
    }

    /**
     * Lấy danh sách ảnh của một biến thể cụ thể, sắp theo ordinalNum.
     */
    public static List<Image> getImagesByProduct(String productId) throws SQLException {
        if (isNullOrEmpty(productId)) {
            return new ArrayList<>();
        }
        return ProductDAO.findImagesByProduct(productId.trim());
    }

    /**
     * Lấy các thuộc tính (size, màu...) của một biến thể.
     */
    public static List<AttributeValue> getAttributes(String productId) throws SQLException {
        if (isNullOrEmpty(productId)) {
            return new ArrayList<>();
        }
        return ProductDAO.findAttributesByProduct(productId.trim());
    }

    // ===== CRUD nhóm sản phẩm (Admin) =====

    /**
     * [Admin] Thêm nhóm sản phẩm + ghi nhật ký.
     */
    public static boolean addGroup(AccountService accountService, GroupProduct group) throws SQLException {
        if (group == null
                || isNullOrEmpty(group.getName())
                || isNullOrEmpty(group.getCategoryId())
                || isNullOrEmpty(group.getBrandId())) {
            return false;
        }

        // Chuẩn hóa chuỗi
        group.setName(group.getName().trim());
        group.setCategoryId(group.getCategoryId().trim());
        group.setBrandId(group.getBrandId().trim());

        // Tự sinh UUID nếu chưa có
        if (isNullOrEmpty(group.getId())) {
            group.setId(UUID.randomUUID().toString());
        } else {
            group.setId(group.getId().trim());
        }

        boolean success = ProductDAO.insertGroup(group);
        if (success) {
            logAdjust(accountService, "Thêm", group.getName(), "Nhóm sản phẩm", group.getId());
        }
        return success;
    }

    /**
     * [Admin] Sửa nhóm sản phẩm + ghi nhật ký.
     */
    public static boolean updateGroup(AccountService accountService, GroupProduct group) throws SQLException {
        if (group == null
                || isNullOrEmpty(group.getId())
                || isNullOrEmpty(group.getName())
                || isNullOrEmpty(group.getCategoryId())
                || isNullOrEmpty(group.getBrandId())) {
            return false;
        }

        group.setId(group.getId().trim());
        group.setName(group.getName().trim());
        group.setCategoryId(group.getCategoryId().trim());
        group.setBrandId(group.getBrandId().trim());

        boolean success = ProductDAO.updateGroup(group);
        if (success) {
            logAdjust(accountService, "Sửa", group.getName(), "Nhóm sản phẩm", group.getId());
        }
        return success;
    }

    /**
     * [Admin] Xóa nhóm sản phẩm + ghi nhật ký.
     */
    public static boolean deleteGroup(AccountService accountService, String groupId) throws SQLException {
        if (isNullOrEmpty(groupId)) {
            return false;
        }

        String id = groupId.trim();
        GroupProduct group = ProductDAO.findGroupById(id);
        String groupName = (group != null) ? group.getName() : id;

        boolean success = ProductDAO.deleteGroup(id);
        if (success) {
            logAdjust(accountService, "Xóa", groupName, "Nhóm sản phẩm", id);
        }
        return success;
    }

    // ===== CRUD biến thể (Admin) =====

    /**
     * [Admin] Thêm biến thể (giá, tồn kho, isActive) + ghi nhật ký.
     */
    public static boolean addVariant(AccountService accountService, Product product) throws SQLException {
        if (product == null
                || isNullOrEmpty(product.getGroupProductId())
                || product.getPrice() < 0
                || product.getStockQuantity() < 0) {
            return false;
        }

        product.setGroupProductId(product.getGroupProductId().trim());

        // Tự sinh UUID nếu chưa có
        if (isNullOrEmpty(product.getId())) {
            product.setId(UUID.randomUUID().toString());
        } else {
            product.setId(product.getId().trim());
        }

        boolean success = ProductDAO.insertVariant(product);
        if (success) {
            logAdjust(accountService, "Thêm", getVariantLogName(product), "Biến thể sản phẩm", product.getId());
        }
        return success;
    }

    /**
     * [Admin] Sửa biến thể (kể cả bật/tắt isActive) + ghi nhật ký.
     */
    public static boolean updateVariant(AccountService accountService, Product product) throws SQLException {
        if (product == null
                || isNullOrEmpty(product.getId())
                || product.getPrice() < 0
                || product.getStockQuantity() < 0) {
            return false;
        }

        product.setId(product.getId().trim());

        boolean success = ProductDAO.updateVariant(product);
        if (success) {
            logAdjust(accountService, "Sửa", getVariantLogName(product), "Biến thể sản phẩm", product.getId());
        }
        return success;
    }

    /**
     * [Admin] Xóa biến thể + ghi nhật ký.
     */
    public static boolean deleteVariant(AccountService accountService, String productId) throws SQLException {
        if (isNullOrEmpty(productId)) {
            return false;
        }

        String id = productId.trim();
        Product existing = ProductDAO.findVariantById(id);
        String objectName = (existing != null) ? getVariantLogName(existing) : id;

        boolean success = ProductDAO.deleteVariant(id);
        if (success) {
            logAdjust(accountService, "Xóa", objectName, "Biến thể sản phẩm", id);
        }
        return success;
    }

    /**
     * [Admin] Lấy các biến thể có tồn kho dưới ngưỡng cảnh báo.
     */
    public static List<Product> getLowStock(int threshold) throws SQLException {
        if (threshold < 0) {
            return new ArrayList<>();
        }
        return ProductDAO.findLowStock(threshold);
    }

    // ===== Thuộc tính & ảnh (Admin) =====

    /**
     * [Admin] Thêm giá trị thuộc tính mới (vd: Size = M).
     */
    public static boolean addAttributeValue(AccountService accountService, AttributeValue attr) throws SQLException {
        if (attr == null || isNullOrEmpty(attr.getName()) || isNullOrEmpty(attr.getValue())) {
            return false;
        }

        attr.setName(attr.getName().trim());
        attr.setValue(attr.getValue().trim());

        // Tự sinh UUID nếu chưa có
        if (isNullOrEmpty(attr.getId())) {
            attr.setId(UUID.randomUUID().toString());
        } else {
            attr.setId(attr.getId().trim());
        }

        boolean success = ProductDAO.insertAttributeValue(attr);
        if (success) {
            logAdjust(accountService, "Thêm", attr.getName() + ": " + attr.getValue(), "Thuộc tính", attr.getId());
        }
        return success;
    }

    /**
     * [Admin] Sửa giá trị thuộc tính.
     */
    public static boolean updateAttributeValue(AccountService accountService, AttributeValue attr) throws SQLException {
        if (attr == null
                || isNullOrEmpty(attr.getId())
                || isNullOrEmpty(attr.getName())
                || isNullOrEmpty(attr.getValue())) {
            return false;
        }

        attr.setId(attr.getId().trim());
        attr.setName(attr.getName().trim());
        attr.setValue(attr.getValue().trim());

        boolean success = ProductDAO.updateAttributeValue(attr);
        if (success) {
            logAdjust(accountService, "Sửa", attr.getName() + ": " + attr.getValue(), "Thuộc tính", attr.getId());
        }
        return success;
    }

    /**
     * [Admin] Xóa giá trị thuộc tính (xóa luôn liên kết ProductAttribute trong bảng
     * Have).
     */
    public static boolean deleteAttributeValue(AccountService accountService, String attributeValueId)
            throws SQLException {
        if (isNullOrEmpty(attributeValueId)) {
            return false;
        }

        String id = attributeValueId.trim();
        boolean success = ProductDAO.deleteAttributeValue(id);
        if (success) {
            logAdjust(accountService, "Xóa", id, "Thuộc tính", id);
        }
        return success;
    }

    /**
     * [Admin] Gắn thuộc tính vào biến thể (thêm ProductAttribute vào bảng Have).
     */
    public static boolean linkAttribute(String productId, String attributeValueId) throws SQLException {
        if (isNullOrEmpty(productId) || isNullOrEmpty(attributeValueId)) {
            return false;
        }
        return ProductDAO.linkAttribute(productId.trim(), attributeValueId.trim());
    }

    /**
     * [Admin] Gỡ thuộc tính khỏi biến thể (xóa khỏi bảng Have).
     */
    public static boolean unlinkAttribute(String productId, String attributeValueId) throws SQLException {
        if (isNullOrEmpty(productId) || isNullOrEmpty(attributeValueId)) {
            return false;
        }
        return ProductDAO.unlinkAttribute(productId.trim(), attributeValueId.trim());
    }

    /**
     * [Admin] Thêm ảnh cho sản phẩm.
     */
    public static boolean addImage(AccountService accountService, Image image) throws SQLException {
        if (image == null
                || isNullOrEmpty(image.getName())
                || isNullOrEmpty(image.getUrl())
                || isNullOrEmpty(image.getProductId())) {
            return false;
        }

        image.setName(image.getName().trim());
        image.setUrl(image.getUrl().trim());
        image.setProductId(image.getProductId().trim());

        boolean success = ProductDAO.insertImage(image);
        if (success) {
            logAdjust(accountService, "Thêm", image.getName(), "Hình ảnh", image.getProductId());
        }
        return success;
    }

    /**
     * [Admin] Cập nhật thông tin ảnh (URL hoặc thứ tự hiển thị).
     */
    public static boolean updateImage(AccountService accountService, Image image) throws SQLException {
        if (image == null
                || isNullOrEmpty(image.getName())
                || isNullOrEmpty(image.getUrl())
                || isNullOrEmpty(image.getProductId())) {
            return false;
        }

        image.setName(image.getName().trim());
        image.setUrl(image.getUrl().trim());
        image.setProductId(image.getProductId().trim());

        boolean success = ProductDAO.updateImage(image);
        if (success) {
            logAdjust(accountService, "Sửa", image.getName(), "Hình ảnh", image.getProductId());
        }
        return success;
    }

    /**
     * [Admin] Xóa ảnh theo khóa (productId, name).
     */
    public static boolean deleteImage(AccountService accountService, String productId, String name)
            throws SQLException {
        if (isNullOrEmpty(productId) || isNullOrEmpty(name)) {
            return false;
        }

        String pId = productId.trim();
        String imgName = name.trim();

        boolean success = ProductDAO.deleteImage(pId, imgName);
        if (success) {
            logAdjust(accountService, "Xóa", imgName, "Hình ảnh", pId);
        }
        return success;
    }

    // ===== Helpers =====

    /**
     * Tạo tên hiển thị rõ ràng cho biến thể khi ghi log (VD: "Logitech MX Master 3S
     * (p11)").
     */
    private static String getVariantLogName(Product product) {
        try {
            if (!isNullOrEmpty(product.getGroupProductId())) {
                GroupProduct group = ProductDAO.findGroupById(product.getGroupProductId().trim());
                if (group != null && !isNullOrEmpty(group.getName())) {
                    return group.getName() + " (" + product.getId() + ")";
                }
            }
        } catch (Exception ignored) {
        }
        return product.getId();
    }

    /**
     * Helper ghi nhật ký Admin an toàn (không làm gián đoạn luồng nghiệp vụ chính
     * nếu ghi log thất bại).
     */
    private static void logAdjust(AccountService accountService, String typeAdjust, String objectName,
            String typeObject, String objectId) {
        try {
            AdjustService.log(accountService, typeAdjust, objectName, typeObject, objectId);
        } catch (Exception e) {
            System.err.println("Ghi nhật ký điều chỉnh không thành công: " + e.getMessage());
        }
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
