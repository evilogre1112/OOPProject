package com.oop.DAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class StatisticDAO {

    /** Doanh thu theo ngày trong [from, to]: SUM(Final_amount) của đơn "Hoàn thành", GROUP BY DATE(Created_at). */
    public static Map<String, Double> revenueByDay(LocalDate from, LocalDate to) throws SQLException {
        return null;
    }

    /** Doanh thu theo tháng của một năm: GROUP BY MONTH(Created_at). */
    public static Map<String, Double> revenueByMonth(int year) throws SQLException {
        return null;
    }

    /** Doanh thu theo năm: GROUP BY YEAR(Created_at). */
    public static Map<String, Double> revenueByYear() throws SQLException {
        return null;
    }

    /** Top sản phẩm bán chạy: SUM(Quantity) từ Other_detail của đơn "Hoàn thành", key = tên nhóm sản phẩm. */
    public static Map<String, Integer> topSellingProducts(int limit) throws SQLException {
        return null;
    }

    /** Số đơn theo từng Order_status: GROUP BY Order_status. */
    public static Map<String, Integer> orderCountByStatus() throws SQLException {
        return null;
    }
}
