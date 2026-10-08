package com.oop.Model;

import java.util.Objects;

public class Product {

    private String id;
    private double price;
    private boolean isActive;
    private int stockQuantity;
    private String groupProductId;

    public Product() {
    }

    public Product(String id, double price, boolean isActive, int stockQuantity, String groupProductId) {
        this.id = id;
        this.price = price;
        this.isActive = isActive;
        this.stockQuantity = stockQuantity;
        this.groupProductId = groupProductId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Product))
            return false;
        Product other = (Product) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", price=" + price + ", isActive=" + isActive + ", stockQuantity="
                + stockQuantity + ", groupProductId=" + groupProductId + "}";
    }
}
