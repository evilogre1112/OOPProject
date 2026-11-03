package com.oop.Controller.Admin;

import java.util.ArrayList;
import java.util.List;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Model.Account;
import com.oop.Service.AccountService;

/** Trang Tài khoản: khóa / mở khóa, đặt lại mật khẩu, tạo admin mới. */
public class AccountAdminPanel extends SectionPanel {
    private static final String PASSWORD_RULE = "Mật khẩu tối thiểu 12 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt";

    private final AccountService accountService;
    private final DataTable table = new DataTable("Email", "Tên hiển thị", "Số điện thoại", "Quyền", "Trạng thái");
    private List<Account> accounts = new ArrayList<>();

    public AccountAdminPanel(AccountService accountService) {
        super("Tài khoản");
        this.accountService = accountService;

        bar().button("Khóa / Mở khóa", this::onToggleStatus);
        bar().button("Đặt lại mật khẩu", this::onResetPassword);
        bar().button("Tạo admin", this::onCreateAdmin);
        setBody(table);
        reload();
    }

    private void reload() {
        Async.run(this, accountService::getAllAccounts, list -> {
            accounts = list;
            List<Object[]> rows = new ArrayList<>();
            for (Account a : list) {
                rows.add(new Object[] { a.getEmail(), a.getNickName(), a.getPhoneNum(), a.getRole(),
                        a.isStatus() ? "Hoạt động" : "Bị khóa" });
            }
            table.setRows(rows);
        });
    }

    private void onToggleStatus() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Account account = accounts.get(i);
        boolean newStatus = !account.isStatus();
        if (!newStatus) {
            if (account.getEmail().equals(accountService.getCurrentAccount().getEmail())) {
                Dialogs.error(this, "Không thể khóa chính tài khoản đang đăng nhập.");
                return;
            }
            if (!Dialogs.confirm(this, "Khóa tài khoản " + account.getEmail() + "?")) {
                return;
            }
        }
        Async.action(this, () -> accountService.setAccountStatus(account.getEmail(), newStatus), this::reload);
    }

    private void onResetPassword() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Account account = accounts.get(i);
        String[] v = new FormDialog(this, "Đặt lại mật khẩu")
                .password("Mật khẩu mới cho " + account.getEmail())
                .showDialog();
        if (v == null) {
            return;
        }
        if (!passwordValid(v[0])) {
            Dialogs.error(this, PASSWORD_RULE);
            return;
        }
        Async.action(this, () -> accountService.resetPassword(account.getEmail(), v[0]),
                () -> Dialogs.info(this, "Đã đặt lại mật khẩu."));
    }

    /**
     * validate() kiểm tra cả email lẫn SĐT nên truyền hai giá trị hợp lệ giả để chỉ
     * còn kiểm tra mật khẩu.
     */
    private static boolean passwordValid(String password) {
        return AccountService.validate("a@b.co", "0123456789", password) == null;
    }

    private void onCreateAdmin() {
        String[] v = new FormDialog(this, "Tạo tài khoản admin")
                .text("Email", "")
                .text("Tên hiển thị", "")
                .text("Số điện thoại", "")
                .password("Mật khẩu")
                .combo("Giới tính", new String[] { "Nam", "Nữ" }, 0)
                .showDialog();
        if (v == null) {
            return;
        }
        if (v[1].isEmpty()) {
            Dialogs.error(this, "Vui lòng nhập tên hiển thị");
            return;
        }
        String error = AccountService.validate(v[0], v[2], v[3]);
        if (error != null) {
            Dialogs.error(this, error.startsWith("Mật khẩu") ? PASSWORD_RULE : error);
            return;
        }
        Account admin = new Account();
        admin.setEmail(v[0]);
        admin.setNickName(v[1]);
        admin.setPhoneNum(v[2]);
        admin.setPassword(v[3]); // mật khẩu thô, service/DAO sẽ hash
        admin.setStatus(true);
        admin.setRole("Admin");
        boolean male = "0".equals(v[4]);
        Async.action(this, () -> accountService.createAdmin(admin, male), this::reload);
    }
}