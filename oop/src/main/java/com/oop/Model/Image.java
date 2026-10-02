package com.oop.Model;

import java.util.Objects;

public class Image {

    private String name;
    private String url;
    private int ordinalNum;
    private String productId;

    public Image() {
    }

    public Image(String name, String url, int ordinalNum, String productId) {
        this.name = name;
        this.url = url;
        this.ordinalNum = ordinalNum;
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getOrdinalNum() {
        return ordinalNum;
    }

    public void setOrdinalNum(int ordinalNum) {
        this.ordinalNum = ordinalNum;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Image))
            return false;
        Image other = (Image) o;
        return Objects.equals(productId, other.productId) && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name);
    }

    @Override
    public String toString() {
        return "Image{" + "name=" + name + ", url=" + url + ", ordinalNum=" + ordinalNum + ", productId=" + productId
                + "}";
    }
}
