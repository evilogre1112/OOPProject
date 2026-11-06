package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.oop.Controller.Admin.AdminFrame;
import com.oop.Controller.Common.ProductCardData;
import com.oop.Controller.Common.ScrollablePanel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;
import com.oop.Service.AccountService;
import com.oop.Service.CartService;

/**
 * Trang chủ cửa hàng: header đứng yên, phần thân đổi trang (trang chủ, Shop, chi tiết sản phẩm,
 * giỏ hàng, đơn hàng, địa chỉ, hồ sơ).
 */
public class LobbyFrame extends JFrame {
    private static final int HOME_PRODUCT_LIMIT = 9; // 5 cho hero + 4 cho lưới bên dưới
    private static final int HERO_COUNT = 5;

    private final ScrollablePanel body = new ScrollablePanel();
    private final JScrollPane scroll = new JScrollPane(body);
    private final AccountService accountService;
    private final Account account;
    private final LobbyHeader header;
    private AdminFrame adminFrame; // cửa sổ quản trị (chỉ mở được khi là Admin)

    private JPanel homePage; // giữ lại để quay về không phải tải lại
    private ShopPage shopPage;
    private Runnable listPage = this::showHome; // trang danh sách gần nhất, nút "Quay lại" của trang chi tiết dùng
    private JComponent current; // trang đang hiện

    public LobbyFrame(AccountService accountService, Runnable onLogout) {
        this.accountService = accountService;
        this.account = accountService.getCurrentAccount();

        setTitle("NAKED");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.BG);

        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);

        header = new LobbyHeader(account, () -> {
            if (adminFrame != null) {
                adminFrame.dispose();
            }
            CartService.clear(); // giỏ là static nên phải xóa khi đăng xuất
            dispose();
            onLogout.run();
        }, this::openAdmin, this::showShop, this::showHome, this::showCart,
                this::showOrders, this::showAddresses, this::showProfile);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);

        showHome();
    }

    // ===== Các trang =====

    /** Trang chủ: tải một lần rồi giữ lại. */
    private void showHome() {
        listPage = this::showHome;
        if (homePage != null) {
            showPage(homePage);
            return;
        }
        JPanel loading = showMessage("Đang tải sản phẩm...");
        new SwingWorker<List<ProductCardData>, Void>() {
            @Override
            protected List<ProductCardData> doInBackground() throws Exception {
                return HomeProductLoader.load(HOME_PRODUCT_LIMIT);
            }

            @Override
            protected void done() {
                try {
                    List<ProductCardData> items = get();
                    homePage = items.isEmpty() ? null : buildHome(items);
                    if (current == loading) { // người dùng chưa chuyển sang trang khác
                        if (homePage == null) {
                            showMessage("Chưa có sản phẩm nào.");
                        } else {
                            showPage(homePage);
                        }
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    if (current == loading) {
                        showMessage("Không tải được sản phẩm: " + cause.getMessage());
                    }
                }
            }
        }.execute();
    }

    private JPanel buildHome(List<ProductCardData> items) {
        int heroEnd = Math.min(HERO_COUNT, items.size());

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Theme.BG);
        page.add(new HeroSection(items.subList(0, heroEnd), this::openProduct));
        page.add(new PromoBanner("Bạn hãy kiểm tra các ưu đãi mới nhất"));
        if (items.size() > heroEnd) {
            page.add(new ProductSection("New Arrivals", items.subList(heroEnd, items.size()), this::openProduct));
        }
        return page;
    }

    /** Shop Now: tìm kiếm + lọc, giữ nguyên bộ lọc khi quay lại. */
    private void showShop() {
        listPage = this::showShop;
        if (shopPage == null) {
            shopPage = new ShopPage(this::openProduct);
        }
        showPage(shopPage);
    }

    private void openProduct(ProductCardData data) {
        showPage(new ProductDetailPage(data.groupId, account, listPage, this::refreshCart));
    }

    private void showCart() {
        showPage(new CartPage(account, this::refreshCart, this::showAddresses, this::showOrders));
    }

    private void showOrders() {
        showPage(new OrdersPage(account));
    }

    private void showAddresses() {
        showPage(new AddressPage(account));
    }

    private void showProfile() {
        showPage(new ProfilePage(accountService,
                () -> header.setNickName(accountService.getCurrentAccount().getNickName())));
    }

    // ===== Helper =====

    /** Thay phần thân bằng một trang mới và cuộn lên đầu. */
    private void showPage(JComponent page) {
        current = page;
        body.removeAll();
        body.add(page);
        body.revalidate();
        body.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private JPanel showMessage(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.MUTED);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 120));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(label);

        showPage(wrap);
        return wrap;
    }

    /** Cập nhật số dòng giỏ hàng trên header. */
    private void refreshCart() {
        header.setCartCount(CartService.getItems().size());
    }

    /** Mở cửa sổ quản trị; nếu đang mở rồi thì đưa lên trước. */
    private void openAdmin() {
        if (adminFrame == null || !adminFrame.isDisplayable()) {
            adminFrame = new AdminFrame(accountService);
        }
        adminFrame.setVisible(true);
        adminFrame.toFront();
    }
}
