package com.oop.Controller.Common;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JPanel;

/** Biểu đồ cột đơn giản theo màu NAKED: cột cao nhất màu đỏ đô, còn lại màu đen mực. */
public class BarChart extends JPanel {
    private List<String> labels = new ArrayList<>();
    private List<Double> values = new ArrayList<>();
    private Function<Double, String> format = v -> String.valueOf(v.longValue());

    public BarChart() {
        setBackground(Theme.BG);
        setPreferredSize(new Dimension(0, 260));
    }

    /** labels và values cùng độ dài; format dùng để ghi số trên đầu cột. */
    public void setData(List<String> labels, List<Double> values, Function<Double, String> format) {
        this.labels = labels;
        this.values = values;
        this.format = format;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(Theme.SMALL);
        FontMetrics fm = g2.getFontMetrics();

        int w = getWidth();
        int h = getHeight();
        if (values.isEmpty()) {
            g2.setColor(Theme.MUTED);
            String text = "Chưa có dữ liệu";
            g2.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int top = 28, bottom = 34, side = 24;
        int chartW = w - side * 2;
        int chartH = h - top - bottom;
        int n = values.size();
        double slot = (double) chartW / n;
        int barW = (int) Math.max(2, Math.min(80, slot * 0.6));
        int labelStep = (int) Math.max(1, Math.ceil(64.0 / slot)); // thưa nhãn khi quá nhiều cột

        double max = 0;
        int maxIndex = 0;
        for (int i = 0; i < n; i++) {
            if (values.get(i) > max) {
                max = values.get(i);
                maxIndex = i;
            }
        }
        if (max <= 0) {
            max = 1;
        }

        for (int i = 0; i < n; i++) {
            int barH = (int) Math.round(values.get(i) / max * chartH);
            int x = side + (int) (slot * i + (slot - barW) / 2);
            int y = top + chartH - barH;
            int center = x + barW / 2;

            g2.setColor(i == maxIndex ? Theme.ACCENT : Theme.INK);
            g2.fillRect(x, y, barW, barH);

            if (slot >= 48) { // đủ chỗ mới ghi số trên đầu cột
                String value = format.apply(values.get(i));
                g2.setColor(Theme.INK);
                g2.drawString(value, center - fm.stringWidth(value) / 2, y - 6);
            }
            if (i % labelStep == 0) {
                String label = labels.get(i);
                if (label.length() > 14) {
                    label = label.substring(0, 13) + "…";
                }
                g2.setColor(Theme.MUTED);
                g2.drawString(label, center - fm.stringWidth(label) / 2, h - 12);
            }
        }

        g2.setColor(Theme.LINE);
        g2.drawLine(side, top + chartH, w - side, top + chartH);
        g2.dispose();
    }
}
