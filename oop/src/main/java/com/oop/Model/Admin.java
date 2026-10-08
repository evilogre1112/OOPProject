package com.oop.Model;

public class Admin extends User {

    public Admin() {
    }

    public Admin(String phoneNum, boolean gender) {
        super(phoneNum, gender);
    }

    @Override
    public String toString() {
        return "Admin{" + "phoneNum=" + getPhoneNum() + ", gender=" + getGender() + "}";
    }
}
