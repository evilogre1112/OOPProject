package com.oop.Model;

public class Category extends BaseEntity {

    private String name;

    public Category() {
    }

    public Category(String id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" + "id=" + getId() + ", name=" + name + "}";
    }
}