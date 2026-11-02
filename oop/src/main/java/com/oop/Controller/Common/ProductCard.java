package com.oop.Controller.Common;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Thẻ sản phẩm: ảnh phủ kín, tên (serif nghiêng) + giá nằm góc dưới trái như
 * trên website.
 */
public class ProductCard extends ImagePanel {
    private static final int PAD = 20;

    private final ProductCardData data;
    private final float nameSize;
    private boolean hovering = false;

    public ProductCard(ProductCardData data, float nameSize, Consumer<ProductCardData> onClick) {
        super(data.image, Theme.LINE);
        this.data = data;
        this.nameSize = nameSize;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.accept(data);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // vẽ ảnh
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // dải tối phía dưới để chữ trắng đọc được trên mọi ảnh
        int band = Math.min(h, 140);
        g2.setPaint(new GradientPaint(0, h - band, new Color(0, 0, 0, 0),
                0, h, new Color(0, 0, 0, hovering ? 180 : 120)));
        g2.fillRect(0, h - band, w, band);

        g2.setColor(Color.WHITE);
        g2.setFont(Theme.SERIF_TITLE.deriveFont(nameSize));
        g2.drawString(fit(data.name, g2.getFontMetrics(), w - PAD * 2), PAD, h - 48);

        g2.setFont(Theme.BODY);
        g2.drawString(PriceFormat.vnd(data.price), PAD, h - 22);

        if (hovering) {
            g2.setColor(Theme.ACCENT);
            g2.fillRect(0, h - 4, w, 4);
        }
        g2.dispose();
    }

    /** Cắt bớt tên quá dài và thêm "…". */
    private static String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String cut = text;
        while (cut.length() > 1 && fm.stringWidth(cut + "…") > maxWidth) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "…";
    }
}
