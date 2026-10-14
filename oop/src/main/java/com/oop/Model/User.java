package com.oop.Model;

import java.util.Objects;

public class User {

    private String phoneNum;
    private boolean gender;

    public User() {
    }

    public User(String phoneNum, boolean gender) {
        this.phoneNum = phoneNum;
        this.gender = gender;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public boolean getGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User other = (User) o;
        return Objects.equals(phoneNum, other.phoneNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNum);
    }

    @Override
    public String toString() {
        String Gender = (gender == true) ? "Nam" : "Nu";
        return "User{" + "phoneNum=" + phoneNum + ", gender=" + Gender + "}";
    }
}
