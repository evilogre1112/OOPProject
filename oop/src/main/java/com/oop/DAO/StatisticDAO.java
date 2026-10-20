package com.oop.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oop.DTO.DailyRevenue;
import com.oop.DTO.RevenueDTO;

public class StatisticDAO {

    /**
     * Doanh thu theo ngày trong [from, to]: SUM(Final_amount) của đơn đã thanh toán
     * và chưa huỷ, GROUP BY DATE(Created_at).
     */
    public static List<DailyRevenue> revenueByDay(LocalDate from, LocalDate to) throws SQLException {
        List<DailyRevenue> result = new ArrayList<DailyRevenue>();
        String sql = """
                SELECT
                    DATE(Created_at)  AS order_date,
                    COUNT(*)          AS total_orders,
                    SUM(Final_amount) AS revenue
                FROM Orders
                WHERE Created_at >= ?
                  AND Created_at <  ?
                  AND Payment_status = 'PAID'
                  AND Order_status  <> 'CANCELLED'
                GROUP BY DATE(Created_at)
                ORDER BY order_date
                """;
        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new DailyRevenue(
                            rs.getDate("order_date").toLocalDate(),
                            rs.getInt("total_orders"),
                            rs.getDouble("revenue")));
                }
            }
        }
        return result;
    }

    /** Doanh thu theo tháng của một năm: GROUP BY MONTH(Created_at). */
    public static List<RevenueDTO> revenueByMonth(int year) throws SQLException {
        String sql = """
                SELECT
                    MONTH(Created_at) AS month,
                    COUNT(*)          AS total_orders,
                    SUM(Final_amount) AS revenue
                FROM Orders
                WHERE Created_at >= ?
                  AND Created_at <  ?
                  AND Payment_status = 'PAID'
                  AND Order_status  <> 'CANCELLED'
                GROUP BY MONTH(Created_at)
                ORDER BY month
                """;
        List<RevenueDTO> result = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDate.of(year, 1, 1).atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(LocalDate.of(year + 1, 1, 1).atStartOfDay()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new RevenueDTO(
                            year,
                            rs.getInt("month"),
                            rs.getInt("total_orders"),
                            rs.getDouble("revenue")));
                }
            }
        }
        return result;
    }

    /** Doanh thu theo năm: GROUP BY YEAR(Created_at). */
    public static List<RevenueDTO> revenueByYear() throws SQLException {
        String sql = """
                SELECT
                    YEAR(Created_at)  AS year,
                    COUNT(*)          AS total_orders,
                    SUM(Final_amount) AS revenue
                FROM Orders
                WHERE Payment_status = 'PAID'
                  AND Order_status  <> 'CANCELLED'
                GROUP BY YEAR(Created_at)
                ORDER BY year
                """;

        List<RevenueDTO> result = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(new RevenueDTO(
                        rs.getInt("year"),
                        null, // thống kê theo năm nên không có tháng
                        rs.getInt("total_orders"),
                        rs.getDouble("revenue")));
            }
        }
        return result;
    }

    /**
     * Top sản phẩm bán chạy: SUM(Quantity) từ Other_detail của đơn đã thanh toán
     * và chưa huỷ, key = tên nhóm sản phẩm.
     */
    public static Map<String, Integer> topSellingProducts(int limit) throws SQLException {
        String sql = """
                SELECT
                    gp.Name           AS product_name,
                    SUM(od.Quantity)  AS total_sold
                FROM Other_detail od
                JOIN Orders        o  ON o.Id  = od.Orders_Id
                JOIN Products      p  ON p.Id  = od.Products_Id
                JOIN Group_Product gp ON gp.Id = p.Group_Product_Id
                WHERE o.Payment_status = 'PAID'
                  AND o.Order_status  <> 'CANCELLED'
                GROUP BY gp.Id, gp.Name
                ORDER BY total_sold DESC, gp.Id
                LIMIT ?
                """;

        Map<String, Integer> result = new LinkedHashMap<>();

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("product_name"), rs.getInt("total_sold"));
                }
            }
        }
        return result;
    }

    /** Số đơn theo từng Order_status: GROUP BY Order_status. */
    public static Map<String, Integer> orderCountByStatus() throws SQLException {
        String sql = """
                SELECT
                    Order_status AS status,
                    COUNT(*)     AS total_orders
                FROM Orders
                GROUP BY Order_status
                ORDER BY total_orders DESC, Order_status
                """;

        Map<String, Integer> result = new LinkedHashMap<>();

        try (Connection conn = DBContext.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("status"), rs.getInt("total_orders"));
            }
        }
        return result;
    }
}