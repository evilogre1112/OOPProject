package com.oop.Controller.Admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.BarChart;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.PriceFormat;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Controller.Common.Theme;
import com.oop.DTO.DailyRevenue;
import com.oop.DTO.RevenueDTO;
import com.oop.Service.StatisticService;

/** Trang Thống kê: chọn loại thống kê, bấm Xem để hiện biểu đồ cột và bảng số liệu. */
public class StatisticsPanel extends SectionPanel {
    private static final String[] TYPES = { "Doanh thu theo ngày", "Doanh thu theo tháng", "Doanh thu theo năm",
            "Sản phẩm bán chạy", "Số đơn theo trạng thái" };

    /** Kết quả một lần thống kê: vừa để vẽ biểu đồ (labels/values) vừa để đổ vào bảng (columns/rows). */
    private static class Result {
        final boolean money;
        final String[] columns;
        final List<String> labels = new ArrayList<>();
        final List<Double> values = new ArrayList<>();
        final List<Object[]> rows = new ArrayList<>();

        Result(boolean money, String... columns) {
            this.money = money;
            this.columns = columns;
        }

        void add(String label, double value, Object... cells) {
            labels.add(label);
            values.add(value);
            Object[] row = new Object[cells.length + 1];
            row[0] = label;
            System.arraycopy(cells, 0, row, 1, cells.length);
            rows.add(row);
        }
    }

    private final JComboBox<String> cboType = new JComboBox<>(TYPES);
    private final JTextField txtFrom;
    private final JTextField txtTo;
    private final JTextField txtYear;
    private final JTextField txtTop;
    private final BarChart chart = new BarChart();
    private final DataTable table = new DataTable("Mục", "Giá trị");

    public StatisticsPanel() {
        super("Thống kê");

        cboType.setFont(Theme.BODY);
        cboType.setPreferredSize(new Dimension(210, 38));
        cboType.addActionListener(e -> updateFields());
        bar().addField(cboType);

        LocalDate today = LocalDate.now();
        txtFrom = bar().textField("Từ ngày", today.withDayOfMonth(1).toString(), 100);
        txtTo = bar().textField("Đến ngày", today.toString(), 100);
        txtYear = bar().textField("Năm", String.valueOf(today.getYear()), 60);
        txtTop = bar().textField("Top", "5", 40);
        bar().button("Xem", this::onView);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(chart, BorderLayout.NORTH);
        body.add(table, BorderLayout.CENTER);
        setBody(body);
        updateFields();
    }

    /** Chỉ bật các ô nhập mà loại thống kê đang chọn dùng tới. */
    private void updateFields() {
        int type = cboType.getSelectedIndex();
        txtFrom.setEnabled(type == 0);
        txtTo.setEnabled(type == 0);
        txtYear.setEnabled(type == 1);
        txtTop.setEnabled(type == 3);
    }

    private void onView() {
        Callable<Result> task = buildTask(cboType.getSelectedIndex());
        if (task == null) {
            return; // dữ liệu nhập sai, đã báo lỗi
        }
        Async.run(this, task, this::show);
    }

    /** Đọc tham số trên EDT, trả về việc cần chạy ở luồng nền (null nếu tham số sai). */
    private Callable<Result> buildTask(int type) {
        switch (type) {
            case 0: {
                LocalDate from = Dialogs.date(this, txtFrom.getText(), "Từ ngày");
                LocalDate to = from == null ? null : Dialogs.date(this, txtTo.getText(), "Đến ngày");
                return to == null ? null : () -> byDay(from, to);
            }
            case 1: {
                Integer year = Dialogs.integer(this, txtYear.getText(), "Năm");
                return year == null ? null : () -> byMonth(year);
            }
            case 2:
                return this::byYear;
            case 3: {
                Integer top = Dialogs.integer(this, txtTop.getText(), "Top");
                return top == null ? null : () -> topSelling(top);
            }
            default:
                return this::orderStatus;
        }
    }

    private void show(Result r) {
        chart.setData(r.labels, r.values, r.money ? PriceFormat::compact : v -> String.valueOf(v.longValue()));
        table.setData(r.columns, r.rows);
    }

    // ===== Chạy ở luồng nền =====

    private Result byDay(LocalDate from, LocalDate to) throws Exception {
        Result r = new Result(true, "Ngày", "Số đơn", "Doanh thu");
        for (DailyRevenue d : StatisticService.revenueByDay(from, to)) {
            r.add(d.getDate().toString(), d.getRevenue(), d.getTotalOrders(), PriceFormat.amount(d.getRevenue()));
        }
        return r;
    }

    private Result byMonth(int year) throws Exception {
        Result r = new Result(true, "Tháng", "Số đơn", "Doanh thu");
        for (RevenueDTO d : StatisticService.revenueByMonth(year)) {
            r.add(d.getLabel(), d.getRevenue(), d.getTotalOrders(), PriceFormat.amount(d.getRevenue()));
        }
        return r;
    }

    private Result byYear() throws Exception {
        Result r = new Result(true, "Năm", "Số đơn", "Doanh thu");
        for (RevenueDTO d : StatisticService.revenueByYear()) {
            r.add(d.getLabel(), d.getRevenue(), d.getTotalOrders(), PriceFormat.amount(d.getRevenue()));
        }
        return r;
    }

    private Result topSelling(int limit) throws Exception {
        Result r = new Result(false, "Sản phẩm", "Số lượng bán");
        for (Map.Entry<String, Integer> e : StatisticService.topSellingProducts(limit).entrySet()) {
            r.add(e.getKey(), e.getValue(), e.getValue());
        }
        return r;
    }

    private Result orderStatus() throws Exception {
        Result r = new Result(false, "Trạng thái", "Số đơn");
        for (Map.Entry<String, Integer> e : StatisticService.orderCountByStatus().entrySet()) {
            r.add(e.getKey(), e.getValue(), e.getValue());
        }
        return r;
    }
}
