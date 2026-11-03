package com.oop.Controller.Main;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;

import com.oop.Controller.Common.ProductCard;
import com.oop.Controller.Common.ProductCardData;
import com.oop.Controller.Common.Theme;

/** Khối đầu trang chủ: 1 sản phẩm lớn bên trái + lưới 2x2 bên phải (cần tối đa 5 sản phẩm). */
public class HeroSection extends JPanel {
    private static final int HEIGHT = 640;

    public HeroSection(List<ProductCardData> items, Consumer<ProductCardData> onClick) {
        super(new GridLayout(1, 2));
        setPreferredSize(new Dimension(0, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

        add(cell(items, 0, 36f, onClick));

        JPanel right = new JPanel(new GridLayout(2, 2));
        for (int i = 1; i <= 4; i++) {
            right.add(cell(items, i, 22f, onClick));
        }
        add(right);
    }

    /** Thiếu sản phẩm thì để ô trống màu nền. */
    private JPanel cell(List<ProductCardData> items, int index, float nameSize, Consumer<ProductCardData> onClick) {
        if (index < items.size()) {
            return new ProductCard(items.get(index), nameSize, onClick);
        }
        JPanel empty = new JPanel();
        empty.setBackground(Theme.BG);
        return empty;
    }
}
