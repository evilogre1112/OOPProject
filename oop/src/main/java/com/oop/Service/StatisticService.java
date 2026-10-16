package com.oop.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.oop.DAO.StatisticDAO;
import com.oop.DTO.DailyRevenue;
import com.oop.DTO.RevenueDTO;

public class StatisticService {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 9999 - 1;
    private static final String SQLErrorMessage = "Không có dữ liệu hoặc lỗi truy vấn Database";

    /**
     * Doanh thu từng ngày trong khoảng [from, to]; key = ngày (yyyy-MM-dd),
     * value = doanh thu. Ngày không có đơn được điền 0.0 để vẽ biểu đồ liên tục.
     */
    public static List<DailyRevenue> revenueByDay(LocalDate from, LocalDate to) throws SQLException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải <= ngày kết thúc");
        }

        List<DailyRevenue> result = StatisticDAO.revenueByDay(from, to);
        if (result.isEmpty())
            throw new SQLException(SQLErrorMessage);
        return result;
    }

    /**
     * Doanh thu từng tháng của một năm; key = tháng ("1".."12"), value = doanh thu.
     * Tháng không có đơn được điền 0.0.
     */
    public static List<RevenueDTO> revenueByMonth(int year) throws SQLException {
        checkYear(year);

        List<RevenueDTO> result = StatisticDAO.revenueByMonth(year);
        if (result.isEmpty())
            throw new SQLException(SQLErrorMessage);
        return result;
    }

    /** Doanh thu từng năm (tăng dần); key = năm, value = doanh thu. */
    public static List<RevenueDTO> revenueByYear() throws SQLException {
        List<RevenueDTO> result = StatisticDAO.revenueByYear();
        if (result.isEmpty())
            throw new SQLException(SQLErrorMessage);
        return result;
    }

    /**
     * Top sản phẩm bán chạy: key = tên nhóm sản phẩm, value = tổng số lượng bán
     * (giảm dần).
     */
    public static Map<String, Integer> topSellingProducts(int limit) throws SQLException {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit phải > 0");
        }
        Map<String, Integer> result = StatisticDAO.topSellingProducts(limit);
        if (result.isEmpty())
            throw new SQLException(SQLErrorMessage);
        return result;
    }

    /** Số đơn theo từng trạng thái (cho bảng/biểu đồ phân bổ). */
    public static Map<String, Integer> orderCountByStatus() throws SQLException {
        Map<String, Integer> result = StatisticDAO.orderCountByStatus();
        if (result.isEmpty())
            throw new SQLException(SQLErrorMessage);
        return result;
    }

    private static void checkYear(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("Năm không hợp lệ: " + year);
        }
    }
}