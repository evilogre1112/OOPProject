package com.oop.Controller.Common;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/** Panel vẽ ảnh phủ kín (cắt giữa, giữ tỉ lệ). Không có ảnh thì chỉ tô màu nền. */
public class ImagePanel extends JPanel {
    private final BufferedImage image;
    private BufferedImage scaled; // cache, chỉ vẽ lại khi đổi kích thước

    public ImagePanel(BufferedImage image, Color fallback) {
        this.image = image;
        setBackground(fallback);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // tô màu nền
        int w = getWidth();
        int h = getHeight();
        if (image == null || w <= 0 || h <= 0) {
            return;
        }
        if (scaled == null || scaled.getWidth() != w || scaled.getHeight() != h) {
            scaled = coverScale(w, h);
        }
        g.drawImage(scaled, 0, 0, null);
    }

    private BufferedImage coverScale(int w, int h) {
        double ratio = Math.max((double) w / image.getWidth(), (double) h / image.getHeight());
        int sw = (int) Math.ceil(image.getWidth() * ratio);
        int sh = (int) Math.ceil(image.getHeight() * ratio);

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(image, (w - sw) / 2, (h - sh) / 2, sw, sh, null);
        g2.dispose();
        return out;
    }
}
