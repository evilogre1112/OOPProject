package com.oop.Controller.Common;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Khung một trang quản trị: tiêu đề serif, hàng nút thao tác, rồi phần thân
 * (thường là DataTable).
 */
public class SectionPanel extends JPanel {
    private final ActionBar bar = new ActionBar();

    public SectionPanel(String title) {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.SERIF_TITLE.deriveFont(28f));
        heading.setForeground(Theme.INK);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(bar, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
    }

    public ActionBar bar() {
        return bar;
    }

    public void setBody(JComponent body) {
        add(body, BorderLayout.CENTER);
    }
}
