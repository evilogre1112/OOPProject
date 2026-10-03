package com.oop.Model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Review {

    private String customerPhoneNum;
    private String productId;
    private LocalDateTime createdAt;
    private int rating;
    private String comment;

    public Review() {
    }

    public Review(String customerPhoneNum, String productId, LocalDateTime createdAt, int rating, String comment) {
        this.customerPhoneNum = customerPhoneNum;
        this.productId = productId;
        this.createdAt = createdAt;
        this.rating = rating;
        this.comment = comment;
    }

    public String getCustomerPhoneNum() {
        return customerPhoneNum;
    }

    public void setCustomerPhoneNum(String customerPhoneNum) {
        this.customerPhoneNum = customerPhoneNum;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Review))
            return false;
        Review other = (Review) o;
        return Objects.equals(customerPhoneNum, other.customerPhoneNum) && Objects.equals(productId, other.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerPhoneNum, productId);
    }

    @Override
    public String toString() {
        return "Review{" + "customerPhoneNum=" + customerPhoneNum + ", productId=" + productId + ", createdAt="
                + createdAt + ", rating=" + rating + ", comment=" + comment + "}";
    }
}
