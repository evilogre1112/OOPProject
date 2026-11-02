package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.Controller.Common.ProductCard;
import com.oop.Controller.Common.ProductCardData;
import com.oop.Controller.Common.Theme;

/** Một khối có tiêu đề + lưới sản phẩm 4 cột. */
public class ProductSection extends JPanel {
    private static final int COLUMNS = 4;
    private static final int CARD_HEIGHT = 480;

    public ProductSection(String title, List<ProductCardData> items, Consumer<ProductCardData> onClick) {
        super(new BorderLayout());
        setBackground(Theme.BG);

        JLabel heading = new JLabel(title);
        heading.setFont(Theme.SERIF_TITLE);
        heading.setForeground(Theme.INK);
        heading.setBorder(BorderFactory.createEmptyBorder(40, 32, 24, 32));

        JPanel grid = new JPanel(new GridLayout(0, COLUMNS, 12, 12));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 32, 48, 32));
        for (ProductCardData item : items) {
            ProductCard card = new ProductCard(item, 20f, onClick);
            card.setPreferredSize(new Dimension(300, CARD_HEIGHT));
            grid.add(card);
        }

        add(heading, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }
}
