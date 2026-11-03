package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import com.oop.Controller.Admin.AdminFrame;
import com.oop.Controller.Common.ProductCardData;
import com.oop.Controller.Common.ScrollablePanel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;
import com.oop.Service.AccountService;

/** Trang chủ cửa hàng: header + hero + banner + lưới sản phẩm mới. */
public class LobbyFrame extends JFrame {
    private static final int HOME_PRODUCT_LIMIT = 9; // 5 cho hero + 4 cho lưới bên dưới
    private static final int HERO_COUNT = 5;

    private final ScrollablePanel body = new ScrollablePanel();
    private final AccountService accountService;
    private AdminFrame adminFrame; // cửa sổ quản trị (chỉ mở được khi là Admin)

    public LobbyFrame(AccountService accountService, Runnable onLogout) {
        this.accountService = accountService;
        Account account = accountService.getCurrentAccount();

        setTitle("NAKED");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.BG);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.add(new LobbyHeader(account, () -> {
            if (adminFrame != null) {
                adminFrame.dispose();
            }
            dispose();
            onLogout.run();
        }, this::openAdmin), BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);

        showMessage("Đang tải sản phẩm...");
        loadProducts();
    }

    /** Đọc DB + ảnh ở luồng nền, xong mới dựng giao diện trên EDT. */
    private void loadProducts() {
        new SwingWorker<List<ProductCardData>, Void>() {
            @Override
            protected List<ProductCardData> doInBackground() throws Exception {
                return HomeProductLoader.load(HOME_PRODUCT_LIMIT);
            }

            @Override
            protected void done() {
                try {
                    showProducts(get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    showMessage("Không tải được sản phẩm: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void showProducts(List<ProductCardData> items) {
        if (items.isEmpty()) {
            showMessage("Chưa có sản phẩm nào.");
            return;
        }
        int heroEnd = Math.min(HERO_COUNT, items.size());

        body.removeAll();
        body.add(new HeroSection(items.subList(0, heroEnd), this::openProduct));
        body.add(new PromoBanner("Bạn hãy kiểm tra các ưu đãi mới nhất"));
        if (items.size() > heroEnd) {
            body.add(new ProductSection("New Arrivals", items.subList(heroEnd, items.size()), this::openProduct));
        }
        body.revalidate();
        body.repaint();
    }

    private void showMessage(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.MUTED);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 120));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(label);

        body.removeAll();
        body.add(wrap);
        body.revalidate();
        body.repaint();
    }

    /** Mở cửa sổ quản trị; nếu đang mở rồi thì đưa lên trước. */
    private void openAdmin() {
        if (adminFrame == null || !adminFrame.isDisplayable()) {
            adminFrame = new AdminFrame(accountService);
        }
        adminFrame.setVisible(true);
        adminFrame.toFront();
    }

    /** Tạm thời chỉ hiện hộp thoại; sau này thay bằng mở trang chi tiết sản phẩm. */
    private void openProduct(ProductCardData data) {
        JOptionPane.showMessageDialog(this, data.name + "\n(id: " + data.groupId + ")");
    }
}
