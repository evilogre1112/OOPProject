package com.oop.Model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Adjust {

    private int id;
    private String adminPhoneNum;
    private LocalDateTime createdAt;
    private String typeAdjust;
    private String objectNameAdjusted;
    private String typeObject;
    private String idObjectAdjusted;

    public Adjust() {
    }

    public Adjust(int id, String adminPhoneNum, LocalDateTime createdAt, String typeAdjust, String objectNameAdjusted,
            String typeObject, String idObjectAdjusted) {
        this.id = id;
        this.adminPhoneNum = adminPhoneNum;
        this.createdAt = createdAt;
        this.typeAdjust = typeAdjust;
        this.objectNameAdjusted = objectNameAdjusted;
        this.typeObject = typeObject;
        this.idObjectAdjusted = idObjectAdjusted;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdminPhoneNum() {
        return adminPhoneNum;
    }

    public void setAdminPhoneNum(String adminPhoneNum) {
        this.adminPhoneNum = adminPhoneNum;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTypeAdjust() {
        return typeAdjust;
    }

    public void setTypeAdjust(String typeAdjust) {
        this.typeAdjust = typeAdjust;
    }

    public String getObjectNameAdjusted() {
        return objectNameAdjusted;
    }

    public void setObjectNameAdjusted(String objectNameAdjusted) {
        this.objectNameAdjusted = objectNameAdjusted;
    }

    public String getTypeObject() {
        return typeObject;
    }

    public void setTypeObject(String typeObject) {
        this.typeObject = typeObject;
    }

    public String getIdObjectAdjusted() {
        return idObjectAdjusted;
    }

    public void setIdObjectAdjusted(String idObjectAdjusted) {
        this.idObjectAdjusted = idObjectAdjusted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Adjust))
            return false;
        Adjust other = (Adjust) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Adjust{" + "id=" + id + ", adminPhoneNum=" + adminPhoneNum + ", createdAt=" + createdAt
                + ", typeAdjust=" + typeAdjust + ", objectNameAdjusted=" + objectNameAdjusted + ", typeObject="
                + typeObject + ", idObjectAdjusted=" + idObjectAdjusted + "}";
    }
}
