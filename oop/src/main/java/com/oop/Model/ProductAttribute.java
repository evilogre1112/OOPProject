package com.oop.Model;

import java.util.Objects;

public class ProductAttribute {

    private String attributeValueId;
    private String productId;

    public ProductAttribute() {
    }

    public ProductAttribute(String attributeValueId, String productId) {
        this.attributeValueId = attributeValueId;
        this.productId = productId;
    }

    public String getAttributeValueId() {
        return attributeValueId;
    }

    public void setAttributeValueId(String attributeValueId) {
        this.attributeValueId = attributeValueId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProductAttribute))
            return false;
        ProductAttribute other = (ProductAttribute) o;
        return Objects.equals(attributeValueId, other.attributeValueId) && Objects.equals(productId, other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeValueId, productId);
    }

    @Override
    public String toString() {
        return "ProductAttribute{" + "attributeValueId=" + attributeValueId + ", productId=" + productId + "}";
    }
}
