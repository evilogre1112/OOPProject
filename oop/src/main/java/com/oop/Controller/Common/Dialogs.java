package com.oop.Controller.Common;

import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.JOptionPane;

/** Các hộp thoại thông báo / xác nhận / đọc số dùng chung. */
public final class Dialogs {
    private Dialogs() {
    }

    public static void info(Component owner, String message) {
        JOptionPane.showMessageDialog(owner, message, "NAKED", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component owner, String message) {
        JOptionPane.showMessageDialog(owner, message, "NAKED", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component owner, String message) {
        return JOptionPane.showConfirmDialog(owner, message, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Đọc số thực; sai định dạng thì báo lỗi và trả về null. */
    public static Double number(Component owner, String text, String label) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            error(owner, label + " phải là một số");
            return null;
        }
    }

    /** Đọc ngày dạng yyyy-MM-dd; sai định dạng thì báo lỗi và trả về null. */
    public static LocalDate date(Component owner, String text, String label) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            error(owner, label + " phải theo dạng yyyy-MM-dd, ví dụ 2026-11-03");
            return null;
        }
    }

    /** Đọc số nguyên; sai định dạng thì báo lỗi và trả về null. */
    public static Integer integer(Component owner, String text, String label) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            error(owner, label + " phải là một số nguyên");
            return null;
        }
    }
}
