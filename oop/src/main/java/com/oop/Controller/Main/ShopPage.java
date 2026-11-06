package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.oop.Controller.Common.ActionBar;
import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.NakedButton;
import com.oop.Controller.Common.ProductCardData;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Brand;
import com.oop.Model.Category;
import com.oop.Model.GroupProduct;
import com.oop.Service.CatalogService;
import com.oop.Service.ProductService;

/** Trang Shop Now: tìm theo từ khóa, danh mục, thương hiệu, khoảng giá, sắp xếp; mỗi trang 12 sản phẩm. */
public class ShopPage extends JPanel {
    private static final int PAGE_SIZE = 12;
    private static final String[] SORT_NAMES = { "Mới nhất", "Giá tăng dần", "Giá giảm dần" };
    private static final String[] SORT_KEYS = { "newest", "price_asc", "price_desc" };

    private final Consumer<ProductCardData> onClick;
    private final ActionBar filterBar = new ActionBar();
    private final JPanel results = new JPanel(new BorderLayout());
    private final JTextField txtKeyword;
    private final JTextField txtMin;
    private final JTextField txtMax;
    private final JComboBox<String> cbCategory = new JComboBox<>(new String[] { "Tất cả" });
    private final JComboBox<String> cbBrand = new JComboBox<>(new String[] { "Tất cả" });
    private final JComboBox<String> cbSort = new JComboBox<>(SORT_NAMES);
    private final NakedButton btnPrev;
    private final NakedButton btnNext;
    private final JLabel lblPage = new JLabel(" ");

    private List<Category> categories = new ArrayList<>();
    private List<Brand> brands = new ArrayList<>();
    private int page = 0;
    private int ticket = 0; // bỏ qua kết quả của lần tìm đã cũ

    public ShopPage(Consumer<ProductCardData> onClick) {
        super(new BorderLayout(0, 16));
        this.onClick = onClick;
        setBackground(Theme.BG);

        JLabel heading = new JLabel("Shop Now");
        heading.setFont(Theme.SERIF_TITLE.deriveFont(28f));
        heading.setForeground(Theme.INK);

        txtKeyword = filterBar.textField("Từ khóa", "", 160);
        filterBar.addField(Theme.fieldLabel("Danh mục"));
        filterBar.addField(styled(cbCategory));
        filterBar.addField(Theme.fieldLabel("Thương hiệu"));
        filterBar.addField(styled(cbBrand));
        txtMin = filterBar.textField("Giá từ", "", 90);
        txtMax = filterBar.textField("Đến", "", 90);
        filterBar.addField(Theme.fieldLabel("Sắp xếp"));
        filterBar.addField(styled(cbSort));
        filterBar.button("Tìm", () -> search(0));
        filterBar.button("Đặt lại", this::reset);

        txtKeyword.addActionListener(e -> search(0));
        txtMin.addActionListener(e -> search(0));
        txtMax.addActionListener(e -> search(0));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(28, 32, 0, 32));
        top.add(heading, BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.CENTER);

        results.setOpaque(false);

        lblPage.setFont(Theme.BODY);
        lblPage.setForeground(Theme.MUTED);
        ActionBar pager = new ActionBar();
        btnPrev = pager.button("Trang trước", () -> search(page - 1));
        pager.addField(lblPage);
        btnNext = pager.button("Trang sau", () -> search(page + 1));
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 32, 48, 32));
        bottom.add(pager);

        add(top, BorderLayout.NORTH);
        add(results, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadCatalog();
        search(0);
    }

    /** Nạp danh mục + thương hiệu vào hai ô chọn (thứ tự trong ô = thứ tự trong list). */
    private void loadCatalog() {
        Async.run(this, CatalogService::getAllCategories, list -> {
            categories = list;
            for (Category c : list) {
                cbCategory.addItem(c.getName());
            }
        });
        Async.run(this, CatalogService::getAllBrands, list -> {
            brands = list;
            for (Brand b : list) {
                cbBrand.addItem(b.getName());
            }
        });
    }

    private void reset() {
        txtKeyword.setText("");
        txtMin.setText("");
        txtMax.setText("");
        cbCategory.setSelectedIndex(0);
        cbBrand.setSelectedIndex(0);
        cbSort.setSelectedIndex(0);
        search(0);
    }

    /** Tìm ở luồng nền: lấy PAGE_SIZE + 1 dòng để biết còn trang sau hay không. */
    private void search(int newPage) {
        boolean hasMin = !txtMin.getText().trim().isEmpty();
        boolean hasMax = !txtMax.getText().trim().isEmpty();
        Double min = hasMin ? Dialogs.number(this, txtMin.getText(), "Giá từ") : null;
        Double max = hasMax ? Dialogs.number(this, txtMax.getText(), "Giá đến") : null;
        if ((hasMin && min == null) || (hasMax && max == null)) {
            return;
        }

        int categoryIndex = cbCategory.getSelectedIndex();
        int brandIndex = cbBrand.getSelectedIndex();
        String categoryId = categoryIndex <= 0 ? null : categories.get(categoryIndex - 1).getId();
        String brandId = brandIndex <= 0 ? null : brands.get(brandIndex - 1).getId();
        String keyword = txtKeyword.getText().trim();
        String sort = SORT_KEYS[cbSort.getSelectedIndex()];

        page = newPage;
        int offset = newPage * PAGE_SIZE;
        int mine = ++ticket;
        showMessage("Đang tải sản phẩm...");

        Async.run(this, () -> {
            List<GroupProduct> groups = ProductService.search(categoryId, brandId, min, max, keyword, sort,
                    offset, PAGE_SIZE + 1);
            boolean more = groups.size() > PAGE_SIZE;
            if (more) {
                groups = groups.subList(0, PAGE_SIZE);
            }
            return new Result(HomeProductLoader.toCards(groups), more);
        }, result -> {
            if (mine == ticket) {
                show(result);
            }
        });
    }

    private void show(Result result) {
        results.removeAll();
        if (result.cards.isEmpty()) {
            results.add(message("Không tìm thấy sản phẩm phù hợp."), BorderLayout.CENTER);
        } else {
            results.add(new ProductSection("Tất cả sản phẩm", result.cards, onClick), BorderLayout.CENTER);
        }
        lblPage.setText("Trang " + (page + 1));
        btnPrev.setEnabled(page > 0);
        btnNext.setEnabled(result.more);
        results.revalidate();
        results.repaint();
    }

    private void showMessage(String text) {
        results.removeAll();
        results.add(message(text), BorderLayout.CENTER);
        results.revalidate();
        results.repaint();
    }

    private static JPanel message(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.MUTED);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 120));
        wrap.setOpaque(false);
        wrap.add(label);
        return wrap;
    }

    private static JComboBox<String> styled(JComboBox<String> box) {
        box.setFont(Theme.BODY);
        box.setPreferredSize(new Dimension(150, 38));
        return box;
    }

    /** Một trang kết quả: thẻ sản phẩm + còn trang sau hay không. */
    private static class Result {
        final List<ProductCardData> cards;
        final boolean more;

        Result(List<ProductCardData> cards, boolean more) {
            this.cards = cards;
            this.more = more;
        }
    }
}
