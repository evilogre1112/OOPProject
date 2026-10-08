package com.oop.Model;

public class Customer extends User {

    public Customer() {
    }

    public Customer(String phoneNum, boolean gender) {
        super(phoneNum, gender);
    }

    @Override
    public String toString() {
        return "Customer{" + "phoneNum=" + getPhoneNum() + ", gender=" + getGender() + "}";
    }
}
