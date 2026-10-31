package com.oop.Model;

public class AttributeValue extends BaseEntity {

    private String name;
    private String value;

    public AttributeValue() {
    }

    public AttributeValue(String id, String name, String value) {
        super(id);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AttributeValue{" + "id=" + getId() + ", name=" + name + ", value=" + value + "}";
    }
}