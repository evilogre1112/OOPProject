package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.oop.Controller.Common.ActionBar;
import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.ImagePanel;
import com.oop.Controller.Common.ImageUtil;
import com.oop.Controller.Common.LinkLabel;
import com.oop.Controller.Common.NakedButton;
import com.oop.Controller.Common.PriceFormat;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;
import com.oop.Model.Brand;
import com.oop.Model.Category;
import com.oop.Model.GroupProduct;
import com.oop.Model.Image;
import com.oop.Model.Product;
import com.oop.Model.Review;
import com.oop.Service.CartService;
import com.oop.Service.CatalogService;
import com.oop.Service.ProductService;
import com.oop.Service.ReviewService;

/**
 * Trang chi tiết một nhóm sản phẩm: ảnh theo biến thể, giá, tồn kho, thêm vào giỏ,
 * và phần đánh giá (xem / viết / sửa / xóa đánh giá của chính mình).
 */
public class ProductDetailPage extends JPanel {
    private static final String[] STARS = { "1 sao", "2 sao", "3 sao", "4 sao", "5 sao" };
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int INFO_WIDTH = 480;

    private final String groupId;
    private final Account account;
    private final Runnable onBack;
    private final Runnable onCartChanged;

    private Data data;

    // các thành phần cần cập nhật khi đổi biến thể / tải lại đánh giá
    private JComboBox<String> cbVariant;
    private JLabel lblPrice;
    private JLabel lblStock;
    private JTextField txtQty;
    private NakedButton btnAdd;
    private JPanel imageHolder;
    private CardLayout imageCards;
    private JLabel lblReviews;
    private DataTable tblReviews;

    public ProductDetailPage(String groupId, Account account, Runnable onBack, Runnable onCartChanged) {
        super(new BorderLayout());
        this.groupId = groupId;
        this.account = account;
        this.onBack = onBack;
        this.onCartChanged = onCartChanged;
        setBackground(Theme.BG);
        add(message("Đang tải sản phẩm..."), BorderLayout.CENTER);

        Async.run(this, this::fetch, loaded -> {
            data = loaded;
            render();
        });
    }

    // ===== Đọc dữ liệu (luồng nền) =====

    private Data fetch() throws java.sql.SQLException {
        Data d = new Data();
        d.group = ProductService.getGroupById(groupId);
        if (d.group == null) {
            throw new java.sql.SQLException("Không tìm thấy sản phẩm");
        }
        for (Category c : CatalogService.getAllCategories()) {
            if (c.getId().equals(d.group.getCategoryId())) {
                d.category = c.getName();
            }
        }
        for (Brand b : CatalogService.getAllBrands()) {
            if (b.getId().equals(d.group.getBrandId())) {
                d.brand = b.getName();
            }
        }
        for (Product p : ProductService.getVariants(groupId)) {
            if (p.isActive()) { // biến thể ngừng bán thì không cho chọn
                d.variants.add(p);
            }
        }

        // Ảnh: mỗi biến thể lấy ảnh đầu tiên của nó, không có thì dùng ảnh đầu tiên của cả nhóm
        List<Image> allImages = ProductService.getImages(groupId);
        Map<String, BufferedImage> cache = new HashMap<>();
        BufferedImage fallback = allImages.isEmpty() ? null : loadImage(allImages.get(0).getUrl(), cache);
        int number = 1;
        for (Product p : d.variants) {
            String attributes = VariantNames.attributes(p.getId());
            d.labels.put(p.getId(), attributes.isEmpty() ? "Mẫu " + number : attributes);
            number++;

            BufferedImage image = fallback;
            for (Image i : allImages) {
                if (p.getId().equals(i.getProductId())) {
                    image = loadImage(i.getUrl(), cache);
                    break;
                }
            }
            d.images.put(p.getId(), image);
        }

        d.reviews = fetchReviews();
        return d;
    }

    private ReviewData fetchReviews() throws java.sql.SQLException {
        ReviewData r = new ReviewData();
        r.list = ReviewService.getByGroup(groupId);
        r.average = ReviewService.getAverageRatingByGroup(groupId);
        return r;
    }

    private static BufferedImage loadImage(String path, Map<String, BufferedImage> cache) {
        if (!cache.containsKey(path)) {
            cache.put(path, ImageUtil.load(path));
        }
        return cache.get(path);
    }

    // ===== Dựng giao diện =====

    private void render() {
        removeAll();
        setBorder(BorderFactory.createEmptyBorder(24, 32, 48, 32));
        add(buildTop(), BorderLayout.NORTH);
        add(buildReviews(), BorderLayout.CENTER);
        showVariant();
        revalidate();
        repaint();
    }

    /** Hàng trên: ảnh bên trái, thông tin + nút thêm vào giỏ bên phải. */
    private JPanel buildTop() {
        imageCards = new CardLayout();
        imageHolder = new JPanel(imageCards);
        imageHolder.setPreferredSize(new Dimension(0, 600));
        imageHolder.add(new ImagePanel(null, Theme.LINE), "none");
        for (Product p : data.variants) {
            imageHolder.add(new ImagePanel(data.images.get(p.getId()), Theme.LINE), p.getId());
        }

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        LinkLabel back = new LinkLabel("← Quay lại", onBack);
        back.setAlignmentX(LEFT_ALIGNMENT);
        info.add(back);
        info.add(gap(24));

        List<String> tags = new ArrayList<>();
        if (data.brand != null) {
            tags.add(data.brand);
        }
        if (data.category != null) {
            tags.add(data.category);
        }
        if (!tags.isEmpty()) {
            info.add(Theme.fieldLabel(String.join("  ·  ", tags)));
            info.add(gap(10));
        }

        info.add(html(data.group.getName(), Theme.SERIF_TITLE, Theme.INK));
        info.add(gap(12));

        lblPrice = new JLabel(" ");
        lblPrice.setFont(Theme.BODY.deriveFont(22f));
        lblPrice.setForeground(Theme.INK);
        lblPrice.setAlignmentX(LEFT_ALIGNMENT);
        info.add(lblPrice);
        info.add(gap(16));

        String description = data.group.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            info.add(html(description.trim(), Theme.BODY, Theme.MUTED));
            info.add(gap(24));
        }

        info.add(Theme.fieldLabel("Phân loại"));
        info.add(gap(4));
        cbVariant = new JComboBox<>();
        for (Product p : data.variants) {
            String label = data.labels.get(p.getId());
            cbVariant.addItem(p.getStockQuantity() > 0 ? label : label + " — hết hàng");
        }
        cbVariant.setFont(Theme.BODY);
        cbVariant.setAlignmentX(LEFT_ALIGNMENT);
        cbVariant.setMaximumSize(new Dimension(INFO_WIDTH, 34));
        cbVariant.addActionListener(e -> showVariant());
        info.add(cbVariant);
        info.add(gap(8));

        lblStock = new JLabel(" ");
        lblStock.setFont(Theme.SMALL);
        lblStock.setForeground(Theme.MUTED);
        lblStock.setAlignmentX(LEFT_ALIGNMENT);
        info.add(lblStock);
        info.add(gap(20));

        info.add(Theme.fieldLabel("Số lượng"));
        txtQty = new JTextField("1");
        Theme.styleField(txtQty);
        txtQty.setMaximumSize(new Dimension(100, txtQty.getPreferredSize().height));
        txtQty.setAlignmentX(LEFT_ALIGNMENT);
        info.add(txtQty);
        info.add(gap(20));

        btnAdd = new NakedButton("Thêm vào giỏ");
        btnAdd.setPreferredSize(new Dimension(260, 46));
        btnAdd.setMaximumSize(new Dimension(260, 46));
        btnAdd.addActionListener(e -> onAddToCart());
        info.add(btnAdd);

        JPanel infoWrap = new JPanel(new BorderLayout());
        infoWrap.setOpaque(false);
        infoWrap.add(info, BorderLayout.NORTH);

        JPanel top = new JPanel(new GridLayout(1, 2, 48, 0));
        top.setOpaque(false);
        top.add(imageHolder);
        top.add(infoWrap);
        return top;
    }

    /** Hàng dưới: tiêu đề + điểm trung bình, nút thao tác, bảng đánh giá. */
    private JPanel buildReviews() {
        lblReviews = new JLabel(" ");
        lblReviews.setFont(Theme.SERIF_TITLE.deriveFont(28f));
        lblReviews.setForeground(Theme.INK);

        ActionBar bar = new ActionBar();
        bar.button("Viết / sửa đánh giá", this::onReview);
        bar.button("Xóa đánh giá", this::onDeleteReview);

        JPanel head = new JPanel(new BorderLayout(0, 14));
        head.setOpaque(false);
        head.add(lblReviews, BorderLayout.NORTH);
        head.add(bar, BorderLayout.CENTER);

        tblReviews = new DataTable("Ngày", "Khách hàng", "Đánh giá", "Nhận xét");
        tblReviews.setPreferredSize(new Dimension(0, 260));

        JPanel reviews = new JPanel(new BorderLayout(0, 16));
        reviews.setOpaque(false);
        reviews.setBorder(BorderFactory.createEmptyBorder(48, 0, 0, 0));
        reviews.add(head, BorderLayout.NORTH);
        reviews.add(tblReviews, BorderLayout.CENTER);

        fillReviews();
        return reviews;
    }

    /** Cập nhật giá, tồn kho, ảnh theo biến thể đang chọn (dữ liệu đã tải sẵn, không đụng DB). */
    private void showVariant() {
        Product p = selected();
        if (p == null) {
            lblPrice.setText("Tạm ngừng bán");
            lblStock.setText(" ");
            btnAdd.setEnabled(false);
            imageCards.show(imageHolder, "none");
            return;
        }
        lblPrice.setText(PriceFormat.vnd(p.getPrice()));
        lblStock.setText(p.getStockQuantity() > 0 ? "Còn " + p.getStockQuantity() + " sản phẩm" : "Hết hàng");
        btnAdd.setEnabled(p.getStockQuantity() > 0);
        imageCards.show(imageHolder, p.getId());
    }

    private void fillReviews() {
        List<Review> list = data.reviews.list;
        lblReviews.setText(list.isEmpty() ? "Đánh giá"
                : String.format("Đánh giá  ·  %.1f ★  (%d)", data.reviews.average, list.size()));

        List<Object[]> rows = new ArrayList<>();
        for (Review r : list) {
            int stars = Math.max(0, Math.min(5, r.getRating()));
            rows.add(new Object[] {
                    r.getCreatedAt() == null ? "" : r.getCreatedAt().format(DATE),
                    mask(r.getCustomerPhoneNum()),
                    "★".repeat(stars) + "☆".repeat(5 - stars),
                    r.getComment() });
        }
        tblReviews.setRows(rows);
    }

    // ===== Thao tác =====

    private void onAddToCart() {
        Product p = selected();
        if (p == null) {
            return;
        }
        Integer qty = Dialogs.integer(this, txtQty.getText(), "Số lượng");
        if (qty == null) {
            return;
        }
        if (!CartService.add(p, qty)) {
            Dialogs.error(this, "Không thể thêm vào giỏ: số lượng không hợp lệ hoặc vượt quá tồn kho (tính cả số đã có trong giỏ).");
            return;
        }
        onCartChanged.run();
        Dialogs.info(this, "Đã thêm vào giỏ hàng.");
    }

    /** Viết đánh giá mới (cần đã mua) hoặc sửa đánh giá cũ cho biến thể đang chọn. */
    private void onReview() {
        Product p = selected();
        if (p == null) {
            return;
        }
        String phone = account.getPhoneNum();
        String productId = p.getId();

        Async.run(this, () -> new boolean[] {
                ReviewService.hasReviewed(phone, productId),
                ReviewService.hasPurchased(phone, productId) }, flags -> {
                    boolean reviewed = flags[0];
                    if (!reviewed && !flags[1]) {
                        Dialogs.info(this, "Bạn chỉ có thể đánh giá biến thể đã mua (đơn hàng đã hoàn thành).");
                        return;
                    }
                    Review old = reviewed ? findOwn(productId) : null;
                    String[] v = new FormDialog(this, reviewed ? "Sửa đánh giá" : "Viết đánh giá")
                            .combo("Số sao", STARS, old == null ? 4 : old.getRating() - 1)
                            .text("Nhận xét", old == null ? "" : old.getComment())
                            .showDialog();
                    if (v == null) {
                        return;
                    }
                    Review review = new Review();
                    review.setCustomerPhoneNum(phone);
                    review.setProductId(productId);
                    review.setRating(Integer.parseInt(v[0]) + 1); // combo trả về chỉ số
                    review.setComment(v[1]);
                    Async.action(this, () -> reviewed ? ReviewService.update(review) : ReviewService.add(review),
                            this::reloadReviews);
                });
    }

    private void onDeleteReview() {
        Product p = selected();
        if (p == null) {
            return;
        }
        String phone = account.getPhoneNum();
        String productId = p.getId();
        if (findOwn(productId) == null) {
            Dialogs.info(this, "Bạn chưa đánh giá biến thể này.");
            return;
        }
        if (!Dialogs.confirm(this, "Xóa đánh giá của bạn cho biến thể này?")) {
            return;
        }
        Async.action(this, () -> ReviewService.delete(phone, productId), this::reloadReviews);
    }

    /** Chỉ tải lại phần đánh giá để trang không bị nhảy lên đầu. */
    private void reloadReviews() {
        Async.run(this, this::fetchReviews, r -> {
            data.reviews = r;
            fillReviews();
        });
    }

    // ===== Helper =====

    private Product selected() {
        int index = cbVariant == null ? -1 : cbVariant.getSelectedIndex();
        return index < 0 ? null : data.variants.get(index);
    }

    /** Đánh giá của chính khách này cho một biến thể (trong danh sách đã tải); không có thì null. */
    private Review findOwn(String productId) {
        for (Review r : data.reviews.list) {
            if (account.getPhoneNum().equals(r.getCustomerPhoneNum()) && productId.equals(r.getProductId())) {
                return r;
            }
        }
        return null;
    }

    /** Ẩn bớt số điện thoại của người đánh giá: *******123. */
    private static String mask(String phone) {
        if (phone == null || phone.length() <= 3) {
            return phone == null ? "" : phone;
        }
        return "*".repeat(phone.length() - 3) + phone.substring(phone.length() - 3);
    }

    /** Nhãn tự xuống dòng theo chiều rộng cột thông tin. */
    private static JLabel html(String text, Font font, Color color) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        JLabel label = new JLabel("<html><div style='width:" + INFO_WIDTH + "px'>" + safe + "</div></html>");
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static JComponent gap(int height) {
        return (JComponent) Box.createRigidArea(new Dimension(0, height));
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

    /** Điểm trung bình + danh sách đánh giá của cả nhóm. */
    private static class ReviewData {
        List<Review> list = new ArrayList<>();
        double average;
    }

    /** Mọi thứ trang cần, tải một lần ở luồng nền. */
    private static class Data {
        GroupProduct group;
        String category;
        String brand;
        List<Product> variants = new ArrayList<>(); // chỉ biến thể đang bán
        Map<String, String> labels = new HashMap<>(); // productId -> "Size: M, Màu: Đỏ"
        Map<String, BufferedImage> images = new HashMap<>(); // productId -> ảnh
        ReviewData reviews;
    }
}
