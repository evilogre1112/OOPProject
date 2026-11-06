package com.oop.Controller.Main;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormField;
import com.oop.Controller.Common.NakedButton;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;
import com.oop.Service.AccountService;

/** Tài khoản của khách: sửa nickname + giới tính, đổi mật khẩu. Email và SĐT chỉ để xem. */
public class ProfilePage extends SectionPanel {
    private static final String[] GENDERS = { "Nam", "Nữ" }; // chỉ số 0 = true (Nam), 1 = false (Nữ)

    private final AccountService accountService;
    private final Runnable onProfileChanged; // cập nhật lời chào trên header

    private final JTextField txtNick;
    private final FormField nickForm;
    private final JComboBox<String> cbGender = new JComboBox<>(GENDERS);

    private final JPasswordField txtOld = new JPasswordField(24);
    private final JPasswordField txtNew = new JPasswordField(24);
    private final JPasswordField txtConfirm = new JPasswordField(24);
    private final FormField oldForm = new FormField("Mật khẩu hiện tại", txtOld);
    private final FormField newForm = new FormField("Mật khẩu mới", txtNew);
    private final FormField confirmForm = new FormField("Nhập lại mật khẩu mới", txtConfirm);

    public ProfilePage(AccountService accountService, Runnable onProfileChanged) {
        super("Tài khoản");
        this.accountService = accountService;
        this.onProfileChanged = onProfileChanged;
        Account account = accountService.getCurrentAccount();

        // ----- Cột trái: hồ sơ -----
        JTextField txtEmail = new JTextField(account.getEmail(), 24);
        JTextField txtPhone = new JTextField(account.getPhoneNum(), 24);
        txtNick = new JTextField(account.getNickName(), 24);

        JPanel profile = column("Hồ sơ");
        profile.add(readOnly("Email", txtEmail));
        profile.add(readOnly("Số điện thoại", txtPhone));
        nickForm = new FormField("Nickname", txtNick);
        profile.add(nickForm);

        cbGender.setFont(Theme.BODY);
        cbGender.setAlignmentX(LEFT_ALIGNMENT);
        cbGender.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        profile.add(Theme.fieldLabel("Giới tính"));
        profile.add(gap(4));
        profile.add(cbGender);
        profile.add(gap(24));
        profile.add(button("Lưu thay đổi", this::onSaveProfile));

        // ----- Cột phải: đổi mật khẩu -----
        JPanel password = column("Đổi mật khẩu");
        password.add(oldForm);
        password.add(newForm);
        password.add(confirmForm);
        password.add(gap(8));
        password.add(button("Đổi mật khẩu", this::onChangePassword));

        JPanel body = new JPanel(new GridLayout(1, 2, 64, 0));
        body.setOpaque(false);
        body.setPreferredSize(new Dimension(900, 460));
        body.add(profile);
        body.add(password);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrap.setOpaque(false);
        wrap.add(body);
        setBody(wrap);

        // Giới tính nằm ở bảng User nên phải đọc DB
        Async.run(this, () -> AccountService.getProfile(account.getPhoneNum()), user -> {
            if (user != null) {
                cbGender.setSelectedIndex(Boolean.TRUE.equals(user.getGender()) ? 0 : 1);
            }
        });
    }

    private void onSaveProfile() {
        String nick = txtNick.getText().trim();
        if (nick.isEmpty()) {
            nickForm.setError("Nickname không được để trống");
            return;
        }
        nickForm.clearError();

        String email = accountService.getCurrentAccount().getEmail();
        boolean gender = cbGender.getSelectedIndex() == 0;
        Async.action(this, () -> accountService.updateProfile(email, nick, gender), () -> {
            Dialogs.info(this, "Đã lưu hồ sơ.");
            onProfileChanged.run();
        });
    }

    private void onChangePassword() {
        String oldPass = new String(txtOld.getPassword());
        String newPass = new String(txtNew.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        oldForm.clearError();
        newForm.clearError();
        confirmForm.clearError();

        Account account = accountService.getCurrentAccount();
        boolean ok = true;
        if (oldPass.isEmpty()) {
            oldForm.setError("Hãy nhập mật khẩu hiện tại");
            ok = false;
        }
        if (AccountService.validate(account.getEmail(), account.getPhoneNum(), newPass) != null) {
            newForm.setError("Ít nhất 12 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt (!@#$%^&*)");
            ok = false;
        }
        if (!newPass.equals(confirm)) {
            confirmForm.setError("Mật khẩu nhập lại không khớp");
            ok = false;
        }
        if (!ok) {
            return;
        }

        Async.run(this, () -> accountService.changePassword(oldPass, newPass), changed -> {
            if (changed) {
                Dialogs.info(this, "Đã đổi mật khẩu.");
                txtOld.setText("");
                txtNew.setText("");
                txtConfirm.setText("");
            } else {
                oldForm.setError("Mật khẩu hiện tại không đúng");
            }
        });
    }

    // ===== Helper =====

    private static JPanel column(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.SERIF_TITLE.deriveFont(22f));
        heading.setForeground(Theme.INK);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(gap(16));
        return panel;
    }

    /** Ô chỉ để xem (email, SĐT không cho sửa). */
    private static FormField readOnly(String label, JTextField field) {
        FormField form = new FormField(label, field);
        field.setEditable(false);
        field.setForeground(Theme.MUTED);
        return form;
    }

    private static NakedButton button(String text, Runnable action) {
        NakedButton button = new NakedButton(text);
        button.setPreferredSize(new Dimension(240, 46));
        button.setMaximumSize(new Dimension(240, 46));
        button.addActionListener(e -> action.run());
        return button;
    }

    private static JComponent gap(int height) {
        return (JComponent) Box.createRigidArea(new Dimension(0, height));
    }
}
