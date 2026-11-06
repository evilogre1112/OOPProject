package com.oop.Controller.Main;

import java.sql.SQLException;

import com.oop.Model.AttributeValue;
import com.oop.Model.GroupProduct;
import com.oop.Model.Product;
import com.oop.Service.ProductService;

/** Tên dễ đọc của một biến thể, ví dụ "Áo thun (Size: M, Màu: Đỏ)". Có đọc DB nên gọi ngoài EDT. */
public final class VariantNames {
    private VariantNames() {
    }

    /** "Tên nhóm (thuộc tính)"; biến thể đã bị xóa thì trả về chính productId. */
    public static String of(String productId) throws SQLException {
        Product product = ProductService.getVariantById(productId);
        if (product == null) {
            return productId;
        }
        GroupProduct group = ProductService.getGroupById(product.getGroupProductId());
        String name = group == null ? productId : group.getName();
        String attributes = attributes(productId);
        return attributes.isEmpty() ? name : name + " (" + attributes + ")";
    }

    /** "Size: M, Màu: Đỏ"; chưa có thuộc tính nào thì trả về chuỗi rỗng. */
    public static String attributes(String productId) throws SQLException {
        StringBuilder text = new StringBuilder();
        for (AttributeValue attr : ProductService.getAttributes(productId)) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(attr.getName()).append(": ").append(attr.getValue());
        }
        return text.toString();
    }
}
