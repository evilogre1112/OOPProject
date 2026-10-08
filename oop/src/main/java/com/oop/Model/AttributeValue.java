package com.oop.Model;

import java.util.Objects;

public class AttributeValue {

    private String id;
    private String name;
    private String value;

    public AttributeValue() {
    }

    public AttributeValue(String id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AttributeValue))
            return false;
        AttributeValue other = (AttributeValue) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AttributeValue{" + "id=" + id + ", name=" + name + ", value=" + value + "}";
    }
}
