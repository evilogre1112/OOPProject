package com.oop.Model;

import java.util.Objects;

public class GroupProduct {

    private String id;
    private String name;
    private String description;
    private String categoryId;
    private String brandId;

    public GroupProduct() {
    }

    public GroupProduct(String id, String name, String description, String categoryId, String brandId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GroupProduct))
            return false;
        GroupProduct other = (GroupProduct) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GroupProduct{" + "id=" + id + ", name=" + name + ", description=" + description + ", categoryId="
                + categoryId + ", brandId=" + brandId + "}";
    }
}
