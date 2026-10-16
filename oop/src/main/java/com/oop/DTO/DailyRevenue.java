package com.oop.DTO;

import java.time.LocalDate;

public class DailyRevenue {
    private final LocalDate date;
    private final int totalOrders;
    private final double revenue;

    public DailyRevenue(LocalDate date, int totalOrders, double revenue) {
        this.date = date;
        this.totalOrders = totalOrders;
        this.revenue = revenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public double getRevenue() {
        return revenue;
    }

    @Override
    public String toString() {
        return date + " | orders=" + totalOrders + " | revenue=" + revenue;
    }
}
