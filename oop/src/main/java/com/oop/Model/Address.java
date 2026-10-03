package com.oop.Model;

import java.util.Objects;

public class Address {

    private String note;
    private String street;
    private String district;
    private String city;
    private boolean isDefault;
    private int ordinalNum;
    private String accountEmail;

    public Address() {
    }

    public Address(String note, String street, String district, String city, boolean isDefault, int ordinalNum,
            String accountEmail) {
        this.note = note;
        this.street = street;
        this.district = district;
        this.city = city;
        this.isDefault = isDefault;
        this.ordinalNum = ordinalNum;
        this.accountEmail = accountEmail;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public int getOrdinalNum() {
        return ordinalNum;
    }

    public void setOrdinalNum(int ordinalNum) {
        this.ordinalNum = ordinalNum;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Address))
            return false;
        Address other = (Address) o;
        return Objects.equals(accountEmail, other.accountEmail) && Objects.equals(ordinalNum, other.ordinalNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountEmail, ordinalNum);
    }

    @Override
    public String toString() {
        return "Address{" + "note=" + note + ", street=" + street + ", district=" + district + ", city=" + city
                + ", isDefault=" + isDefault + ", ordinalNum=" + ordinalNum + ", accountEmail=" + accountEmail + "}";
    }
}