package com.oop.Model;

public class Product extends BaseEntity {

    private double price;
    private boolean isActive;
    private int stockQuantity;
    private String groupProductId;

    public Product() {
    }

    public Product(String id, double price, boolean isActive, int stockQuantity, String groupProductId) {
        super(id);
        this.price = price;
        this.isActive = isActive;
        this.stockQuantity = stockQuantity;
        this.groupProductId = groupProductId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getGroupProductId() {
        return groupProductId;
    }

    public void setGroupProductId(String groupProductId) {
        this.groupProductId = groupProductId;
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + getId() + ", price=" + price + ", isActive=" + isActive + ", stockQuantity="
                + stockQuantity + ", groupProductId=" + groupProductId + "}";
    }
}