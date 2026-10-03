package com.oop.Model;

import java.util.Objects;

public class OrderDetail {

    private String id;
    private int quantity;
    private double unitPrice;
    private String productId;
    private String orderId;

    public OrderDetail() {
    }

    public OrderDetail(String id, int quantity, double unitPrice, String productId, String orderId) {
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.productId = productId;
        this.orderId = orderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrderDetail))
            return false;
        OrderDetail other = (OrderDetail) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderDetail{" + "id=" + id + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", productId="
                + productId + ", orderId=" + orderId + "}";
    }
}
