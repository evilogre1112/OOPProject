package com.oop.Controller.Common;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

/** Màu sắc + font dùng chung cho toàn bộ giao diện (phong cách NAKED). */
public final class Theme {
    private Theme() {
    }

    public static final Color BG = new Color(0xFAF7F2); // kem
    public static final Color INK = new Color(0x1A1A1A); // đen mực
    public static final Color MUTED = new Color(0x8A8580); // xám ấm
    public static final Color LINE = new Color(0xD9D4CE); // đường kẻ mảnh
    public static final Color ACCENT = new Color(0x6B1E3A); // đỏ đô (màu váy trong ảnh)
    public static final Color ERROR = new Color(0xB3261E);

    public static final Font SERIF_TITLE = new Font("Georgia", Font.ITALIC, 34);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font LABEL = new Font("SansSerif", Font.BOLD, 10);

    /** Giãn khoảng cách giữa các chữ (letter-spacing). */
    public static Font spaced(Font font, float tracking) {
        return font.deriveFont(Map.of(TextAttribute.TRACKING, tracking));
    }

    /** Nhãn nhỏ viết hoa, giãn chữ phía trên ô nhập. */
    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(spaced(LABEL, 0.12f));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Biến JTextField / JPasswordField thành ô nhập chỉ có gạch chân. */
    public static void styleField(JTextComponent c) {
        c.setOpaque(false);
        c.setFont(BODY);
        c.setForeground(INK);
        c.setCaretColor(ACCENT);
        setUnderline(c, LINE);
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setUnderline(c, ACCENT);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setUnderline(c, LINE);
            }
        });
    }

    private static void setUnderline(JComponent c, Color color) {
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, color),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)));
    }
}
