package com.oop.Model;

public class Brand extends BaseEntity {

    private String name;
    private String logo;

    public Brand() {
    }

    public Brand(String id, String name, String logo) {
        super(id);
        this.name = name;
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "Brand{" + "id=" + getId() + ", name=" + name + ", logo=" + logo + "}";
    }
}