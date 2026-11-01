package com.oop.Controller.Auth;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.oop.Controller.Common.AuthHeader;
import com.oop.Controller.Common.FormField;
import com.oop.Controller.Common.LinkLabel;
import com.oop.Controller.Common.NakedButton;
import com.oop.Controller.Common.Theme;
import com.oop.EXCEPTION.PermissionException;
import com.oop.Model.Account;
import com.oop.Service.AccountService;

public class RegisterPanel extends JPanel {
    private final Runnable onSuccess;

    private final JTextField txtNickName = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtPhone = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JPasswordField txtConfirm = new JPasswordField();
    private final JRadioButton rbMale = new JRadioButton("Nam", true);
    private final JRadioButton rbFemale = new JRadioButton("Nữ");

    private final FormField nickField = new FormField("Tên hiển thị", txtNickName);
    private final FormField emailField = new FormField("Email", txtEmail);
    private final FormField phoneField = new FormField("Số điện thoại", txtPhone);
    private final FormField passwordField = new FormField("Mật khẩu", txtPassword);
    private final FormField confirmField = new FormField("Nhập lại mật khẩu", txtConfirm);

    public RegisterPanel(Runnable onSuccess, Runnable onGoLogin) {
        this.onSuccess = onSuccess;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        NakedButton btnRegister = new NakedButton("Tạo tài khoản");
        btnRegister.addActionListener(e -> doRegister());
        txtConfirm.addActionListener(e -> doRegister());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ask = new JLabel("Đã có tài khoản?  ");
        ask.setFont(Theme.BODY);
        ask.setForeground(Theme.MUTED);
        footer.add(ask);
        footer.add(new LinkLabel("Đăng nhập", onGoLogin));
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        add(new AuthHeader("Tạo tài khoản", "Trở thành một phần của NAKED."));
        add(Box.createRigidArea(new Dimension(0, 32)));
        add(nickField);
        add(emailField);
        add(phoneField);
        add(passwordField);
        add(confirmField);
        add(createGenderRow());
        add(Box.createRigidArea(new Dimension(0, 24)));
        add(btnRegister);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(footer);
    }

    private JPanel createGenderRow() {
        ButtonGroup group = new ButtonGroup();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        for (JRadioButton rb : new JRadioButton[] { rbMale, rbFemale }) {
            rb.setOpaque(false);
            rb.setFont(Theme.BODY);
            rb.setForeground(Theme.INK);
            group.add(rb);
            row.add(rb);
        }

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.add(Theme.fieldLabel("Giới tính"));
        wrapper.add(Box.createRigidArea(new Dimension(0, 4)));
        wrapper.add(row);
        return wrapper;
    }

    private void doRegister() {
        nickField.clearError();
        emailField.clearError();
        phoneField.clearError();
        passwordField.clearError();
        confirmField.clearError();

        String nick = txtNickName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (nick.isEmpty()) {
            nickField.setError("Vui lòng nhập tên hiển thị");
            return;
        }

        // validate() trả về thông báo lỗi đầu tiên (hoặc null nếu hợp lệ)
        String error = AccountService.validate(email, phone, password);
        if (error != null) {
            if (error.startsWith("Email"))
                emailField.setError(error);
            else if (error.startsWith("Số điện thoại"))
                phoneField.setError(error);
            else
                passwordField.setError("Tối thiểu 12 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            return;
        }
        if (!password.equals(confirm)) {
            confirmField.setError("Mật khẩu nhập lại không khớp");
            return;
        }

        Account account = new Account();
        account.setNickName(nick);
        account.setEmail(email);
        account.setPhoneNum(phone);
        account.setPassword(password); // mật khẩu thô, service/DAO sẽ hash
        account.setStatus(true);

        try {
            boolean success = AccountService.registerForCustomer(account, rbMale.isSelected());
            if (success) {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
                onSuccess.run();
            } else {
                emailField.setError("Không thể đăng ký, email hoặc SĐT có thể đã tồn tại");
            }
        } catch (SQLException | PermissionException ex) {
            emailField.setError(ex.getMessage());
        }
    }

    /** Xoá dữ liệu đã nhập (gọi khi chuyển qua lại giữa các form). */
    public void clear() {
        txtNickName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtPassword.setText("");
        txtConfirm.setText("");
        rbMale.setSelected(true);
        nickField.clearError();
        emailField.clearError();
        phoneField.clearError();
        passwordField.clearError();
        confirmField.clearError();
    }
}
