package com.oop.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class StatisticService {

    /** Doanh thu từng ngày trong khoảng [from, to]; key = ngày, value = doanh thu. */
    public static Map<String, Double> revenueByDay(LocalDate from, LocalDate to) throws SQLException {
        return null;
    }

    /** Doanh thu từng tháng của một năm; key = tháng. */
    public static Map<String, Double> revenueByMonth(int year) throws SQLException {
        return null;
    }

    /** Doanh thu từng năm; key = năm. */
    public static Map<String, Double> revenueByYear() throws SQLException {
        return null;
    }

    /** Top sản phẩm bán chạy: key = id sản phẩm (hoặc tên nhóm), value = tổng số lượng bán. */
    public static Map<String, Integer> topSellingProducts(int limit) throws SQLException {
        return null;
    }

    /** Số đơn theo từng trạng thái (cho bảng/biểu đồ phân bổ). */
    public static Map<String, Integer> orderCountByStatus() throws SQLException {
        return null;
    }
}
