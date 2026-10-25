package com.oop.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order extends BaseEntity {

    private LocalDateTime createdAt;
    private String orderStatus;
    private String paymentStatus;
    private double subtotal;
    private double shippingFee;
    private String customerPhoneNum;
    private List<OrderDetail> details = new ArrayList<>();

    public Order() {
    }

    public Order(String id, LocalDateTime createdAt, String orderStatus, String paymentStatus, double subtotal,
            double shippingFee, String customerPhoneNum) {
        super(id);
        this.createdAt = createdAt;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.customerPhoneNum = customerPhoneNum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getCustomerPhoneNum() {
        return customerPhoneNum;
    }

    public void setCustomerPhoneNum(String customerPhoneNum) {
        this.customerPhoneNum = customerPhoneNum;
    }

    public double getFinalAmount() {
        return subtotal + shippingFee;
    }

    public List<OrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetail> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Order{" + "id=" + getId() + ", createdAt=" + createdAt + ", orderStatus=" + orderStatus
                + ", paymentStatus=" + paymentStatus + ", subtotal=" + subtotal + ", shippingFee=" + shippingFee
                + ", customerPhoneNum=" + customerPhoneNum + "}";
    }
}