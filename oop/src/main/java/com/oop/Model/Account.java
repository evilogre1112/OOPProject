package com.oop.Model;

import java.util.Objects;

public class Account {

    private String email; 
    private String nickName; 
    private String password; 
    private String role; 
    private boolean status; 
    private String phoneNum;

    public Account() {
    }

    public Account(String email, String nickName, String password, String role, boolean status, String phoneNum) {
        this.email = email;
        this.nickName = nickName;
        this.password = password;
        this.role = role;
        this.status = status;
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Account))
            return false;
        Account other = (Account) o;
        return Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Account{" + "email=" + email + ", nickName=" + nickName + ", role=" + role + ", status=" + status
                + ", phoneNum=" + phoneNum + "}";
    }
}
