package com.oop.Model;

public class OrderDetail extends BaseEntity {

    private int quantity;
    private double unitPrice;
    private String productId;
    private String orderId;

    public OrderDetail() {
    }

    public OrderDetail(String id, int quantity, double unitPrice, String productId, String orderId) {
        super(id);
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.productId = productId;
        this.orderId = orderId;
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
    public String toString() {
        return "OrderDetail{" + "id=" + getId() + ", quantity=" + quantity + ", unitPrice=" + unitPrice
                + ", productId=" + productId + ", orderId=" + orderId + "}";
    }
}