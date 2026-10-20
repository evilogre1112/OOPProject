package com.oop.DTO;

import java.util.Objects;

public class RevenueDTO {
    private int year;
    private Integer month; // null nếu thống kê theo năm
    private int totalOrders;
    private double revenue;

    public RevenueDTO() {
    }

    public RevenueDTO(int year, Integer month, int totalOrders, double revenue) {
        this.year = year;
        this.month = month;
        this.totalOrders = totalOrders;
        this.revenue = revenue;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    /** Nhãn hiển thị: "2026-03" hoặc "2026". */
    public String getLabel() {
        return month == null ? String.valueOf(year) : String.format("%d-%02d", year, month);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RevenueDTO))
            return false;
        RevenueDTO that = (RevenueDTO) o;
        return year == that.year && totalOrders == that.totalOrders
                && Double.compare(that.revenue, revenue) == 0
                && Objects.equals(month, that.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, totalOrders, revenue);
    }

    @Override
    public String toString() {
        return getLabel() + " | orders=" + totalOrders + " | revenue=" + revenue;
    }
}
