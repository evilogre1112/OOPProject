package com.oop.Controller.Common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import com.oop.Model.Adjust;
import com.oop.Service.AccountService;
import com.oop.Service.AdjustService;

/**
 * Trang Nhật ký điều chỉnh: lọc theo SĐT admin và khoảng ngày (để trống = bỏ
 * qua).
 */
public class AdjustLogPanel extends SectionPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AccountService accountService;
    private final DataTable table = new DataTable("Thời gian", "Admin (SĐT)", "Thao tác", "Loại", "Tên đối tượng",
            "Mã đối tượng");
    private final JTextField txtPhone;
    private final JTextField txtFrom;
    private final JTextField txtTo;

    public AdjustLogPanel(AccountService accountService) {
        super("Nhật ký");
        this.accountService = accountService;

        txtPhone = bar().textField("SĐT admin", "", 130);
        txtFrom = bar().textField("Từ ngày", "", 110);
        txtTo = bar().textField("Đến ngày", "", 110);
        txtFrom.setToolTipText("yyyy-MM-dd, ví dụ 2026-11-01");
        txtTo.setToolTipText("yyyy-MM-dd, ví dụ 2026-11-30");
        bar().button("Lọc", this::reload);
        bar().button("Tất cả", this::onClear);

        setBody(table);
        reload();
    }

    private void onClear() {
        txtPhone.setText("");
        txtFrom.setText("");
        txtTo.setText("");
        reload();
    }

    private void reload() {
        if (!validDate(txtFrom, "Từ ngày") || !validDate(txtTo, "Đến ngày")) {
            return;
        }
        String phone = txtPhone.getText().trim();
        LocalDate from = dateOf(txtFrom);
        LocalDate to = dateOf(txtTo);

        Async.run(this, () -> AdjustService.filter(accountService, phone.isEmpty() ? null : phone, from, to), list -> {
            List<Object[]> rows = new ArrayList<>();
            for (Adjust a : list) {
                rows.add(new Object[] {
                        a.getCreatedAt() == null ? "" : a.getCreatedAt().format(TIME),
                        a.getAdminPhoneNum(), a.getTypeAdjust(), a.getTypeObject(),
                        a.getObjectNameAdjusted(), a.getIdObjectAdjusted() });
            }
            table.setRows(rows);
        });
    }

    /** Ô trống là hợp lệ (bỏ qua); có chữ thì phải đúng yyyy-MM-dd. */
    private boolean validDate(JTextField field, String label) {
        return field.getText().trim().isEmpty() || Dialogs.date(this, field.getText(), label) != null;
    }

    private LocalDate dateOf(JTextField field) {
        String text = field.getText().trim();
        return text.isEmpty() ? null : LocalDate.parse(text);
    }
}
