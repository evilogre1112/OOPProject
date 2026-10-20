package com.oop;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.oop.DTO.DailyRevenue;
import com.oop.DTO.RevenueDTO;
import com.oop.Service.StatisticService;

public class StatisticServiceTest {

    public static void main(String[] args) {
        testRevenueByDay();
        testRevenueByMonth();
        testRevenueByYear();
        testTopSellingProducts();
        testOrderCountByStatus();
        testInvalidInputs();
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(" " + title);
        System.out.println("==================================================");
    }

    // 1. Doanh thu theo ngày
    private static void testRevenueByDay() {
        printHeader("TEST: revenueByDay (30 ngày gần nhất)");
        try {
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusDays(30);
            System.out.println("Khoảng thời gian: " + from + " -> " + to);

            List<DailyRevenue> list = StatisticService.revenueByDay(from, to);
            System.out.println("Số dòng: " + list.size());
            for (DailyRevenue d : list) {
                System.out.println("  " + d);
            }
        } catch (SQLException e) {
            System.out.println("[SQL] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[Tham số] " + e.getMessage());
        }
    }

    // 2. Doanh thu theo tháng
    private static void testRevenueByMonth() {
        printHeader("TEST: revenueByMonth (năm hiện tại)");
        try {
            int year = LocalDate.now().getYear();
            System.out.println("Năm: " + year);

            List<RevenueDTO> list = StatisticService.revenueByMonth(year);
            System.out.println("Số dòng: " + list.size());
            for (RevenueDTO r : list) {
                System.out.println("  " + r);
            }
        } catch (SQLException e) {
            System.out.println("[SQL] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[Tham số] " + e.getMessage());
        }
    }

    // 3. Doanh thu theo năm
    private static void testRevenueByYear() {
        printHeader("TEST: revenueByYear");
        try {
            List<RevenueDTO> list = StatisticService.revenueByYear();
            System.out.println("Số dòng: " + list.size());
            for (RevenueDTO r : list) {
                System.out.println("  " + r);
            }
        } catch (SQLException e) {
            System.out.println("[SQL] " + e.getMessage());
        }
    }

    // 4. Top sản phẩm bán chạy
    private static void testTopSellingProducts() {
        printHeader("TEST: topSellingProducts (top 5)");
        try {
            Map<String, Integer> map = StatisticService.topSellingProducts(5);
            int rank = 1;
            for (Map.Entry<String, Integer> e : map.entrySet()) {
                System.out.printf("  #%d  %-30s %d%n", rank++, e.getKey(), e.getValue());
            }
        } catch (SQLException e) {
            System.out.println("[SQL] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[Tham số] " + e.getMessage());
        }
    }

    // 5. Số đơn theo trạng thái
    private static void testOrderCountByStatus() {
        printHeader("TEST: orderCountByStatus");
        try {
            Map<String, Integer> map = StatisticService.orderCountByStatus();
            for (Map.Entry<String, Integer> e : map.entrySet()) {
                System.out.printf("  %-20s %d%n", e.getKey(), e.getValue());
            }
        } catch (SQLException e) {
            System.out.println("[SQL] " + e.getMessage());
        }
    }

    // 6. Kiểm tra các tham số không hợp lệ (mong đợi ném IllegalArgumentException)
    private static void testInvalidInputs() {
        printHeader("TEST: tham số không hợp lệ");

        expectIllegalArgument("revenueByDay(null, null)",
                () -> StatisticService.revenueByDay(null, null));

        expectIllegalArgument("revenueByDay(from > to)",
                () -> StatisticService.revenueByDay(LocalDate.of(2025, 12, 31), LocalDate.of(2025, 1, 1)));

        expectIllegalArgument("revenueByMonth(1900)",
                () -> StatisticService.revenueByMonth(1900));

        expectIllegalArgument("revenueByMonth(10000)",
                () -> StatisticService.revenueByMonth(10000));

        expectIllegalArgument("topSellingProducts(0)",
                () -> StatisticService.topSellingProducts(0));

        expectIllegalArgument("topSellingProducts(-3)",
                () -> StatisticService.topSellingProducts(-3));
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void expectIllegalArgument(String name, ThrowingRunnable action) {
        try {
            action.run();
            System.out.println("  [FAIL] " + name + " -> không ném exception");
        } catch (IllegalArgumentException e) {
            System.out.println("  [PASS] " + name + " -> " + e.getMessage());
        } catch (Exception e) {

        }
    }
}