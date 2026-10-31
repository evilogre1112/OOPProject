package com.oop.Model;

public class GroupProduct extends BaseEntity {

    private String name;
    private String description;
    private String categoryId;
    private String brandId;

    public GroupProduct() {
    }

    public GroupProduct(String id, String name, String description, String categoryId, String brandId) {
        super(id);
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.brandId = brandId;
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
    public String toString() {
        return "GroupProduct{" + "id=" + getId() + ", name=" + name + ", description=" + description
                + ", categoryId=" + categoryId + ", brandId=" + brandId + "}";
    }
}