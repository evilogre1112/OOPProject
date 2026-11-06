package com.oop.Controller.Main;

import java.util.ArrayList;
import java.util.List;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Model.Account;
import com.oop.Model.Address;
import com.oop.Service.AddressService;

/** Sổ địa chỉ giao hàng: thêm, sửa, xóa, đặt địa chỉ mặc định. */
public class AddressPage extends SectionPanel {
    private static final String[] YES_NO = { "Không", "Có" };

    private final Account account;
    private final DataTable table = new DataTable("STT", "Số nhà, đường", "Quận / Huyện", "Thành phố",
            "Ghi chú", "Mặc định");
    private List<Address> addresses = new ArrayList<>();

    public AddressPage(Account account) {
        super("Sổ địa chỉ");
        this.account = account;

        bar().button("Thêm", this::onAdd);
        bar().button("Sửa", this::onEdit);
        bar().button("Xóa", this::onDelete);
        bar().button("Đặt mặc định", this::onSetDefault);
        table.onDoubleClick(this::onEdit);

        setBody(table);
        reload();
    }

    /** "Số nhà, đường, Quận, Thành phố" — dùng chung khi chọn địa chỉ lúc thanh toán. */
    public static String describe(Address a) {
        return a.getStreet() + ", " + a.getDistrict() + ", " + a.getCity();
    }

    private void reload() {
        Async.run(this, () -> AddressService.getByEmail(account.getEmail()), list -> {
            addresses = list;
            List<Object[]> rows = new ArrayList<>();
            for (Address a : list) {
                rows.add(new Object[] {
                        a.getOrdinalNum(), a.getStreet(), a.getDistrict(), a.getCity(),
                        a.getNote() == null ? "" : a.getNote(),
                        a.isDefault() ? "Mặc định" : "" });
            }
            table.setRows(rows);
        });
    }

    private void onAdd() {
        String[] v = new FormDialog(this, "Thêm địa chỉ")
                .text("Số nhà, đường", "")
                .text("Quận / Huyện", "")
                .text("Thành phố", "")
                .text("Ghi chú", "")
                .combo("Đặt làm mặc định", YES_NO, 0)
                .showDialog();
        if (v == null || missing(v)) {
            return;
        }
        Address a = new Address();
        a.setAccountEmail(account.getEmail());
        a.setStreet(v[0]);
        a.setDistrict(v[1]);
        a.setCity(v[2]);
        a.setNote(v[3]);
        a.setDefault("1".equals(v[4])); // combo trả về chỉ số; địa chỉ đầu tiên tự là mặc định
        Async.action(this, () -> AddressService.add(a), this::reload);
    }

    private void onEdit() {
        int index = table.requireSelection();
        if (index < 0) {
            return;
        }
        Address old = addresses.get(index);
        String[] v = new FormDialog(this, "Sửa địa chỉ")
                .text("Số nhà, đường", old.getStreet())
                .text("Quận / Huyện", old.getDistrict())
                .text("Thành phố", old.getCity())
                .text("Ghi chú", old.getNote())
                .showDialog();
        if (v == null || missing(v)) {
            return;
        }
        Address a = new Address();
        a.setAccountEmail(account.getEmail());
        a.setOrdinalNum(old.getOrdinalNum()); // khóa (email, ordinalNum) để service tìm đúng dòng
        a.setStreet(v[0]);
        a.setDistrict(v[1]);
        a.setCity(v[2]);
        a.setNote(v[3]);
        Async.action(this, () -> AddressService.update(a), this::reload);
    }

    private void onDelete() {
        int index = table.requireSelection();
        if (index < 0) {
            return;
        }
        Address old = addresses.get(index);
        if (!Dialogs.confirm(this, "Xóa địa chỉ \"" + describe(old) + "\"?")) {
            return;
        }
        Async.action(this, () -> AddressService.delete(account.getEmail(), old.getOrdinalNum()), this::reload);
    }

    private void onSetDefault() {
        int index = table.requireSelection();
        if (index < 0) {
            return;
        }
        Address old = addresses.get(index);
        Async.action(this, () -> AddressService.setDefault(account.getEmail(), old.getOrdinalNum()), this::reload);
    }

    /** Ba ô đầu (đường, quận, thành phố) là bắt buộc. */
    private boolean missing(String[] v) {
        if (v[0].isEmpty() || v[1].isEmpty() || v[2].isEmpty()) {
            Dialogs.error(this, "Hãy nhập đủ số nhà/đường, quận/huyện và thành phố.");
            return true;
        }
        return false;
    }
}
