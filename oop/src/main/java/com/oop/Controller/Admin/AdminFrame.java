package com.oop.Controller.Admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.Controller.Common.AdjustLogPanel;
import com.oop.Controller.Common.NavItem;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;
import com.oop.Service.AccountService;

/**
 * Cửa sổ quản trị: menu bên trái, mỗi mục là một trang (tạo khi bấm lần đầu để
 * không tải DB một lúc).
 */
public class AdminFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final JPanel menu = new JPanel();
    private final Map<String, NavItem> items = new LinkedHashMap<>();
    private final Map<String, Supplier<JComponent>> factories = new LinkedHashMap<>();
    private final Set<String> built = new HashSet<>();

    public AdminFrame(AccountService accountService) {
        Account me = accountService.getCurrentAccount();
        if (me == null || !"Admin".equals(me.getRole())) {
            throw new IllegalStateException("Chỉ Admin mới được mở trang quản trị");
        }

        setTitle("NAKED - Quản trị");
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // chỉ đóng cửa sổ admin, không thoát chương trình

        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(Theme.BG);
        menu.setPreferredSize(new Dimension(240, 0));
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.LINE),
                BorderFactory.createEmptyBorder(28, 0, 28, 0)));

        JLabel brand = new JLabel("NAKED");
        brand.setFont(Theme.spaced(new Font("SansSerif", Font.PLAIN, 22), 0.4f));
        brand.setForeground(Theme.INK);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 24, 4, 24));
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = Theme.fieldLabel("Quản trị");
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 24, 28, 24));
        menu.add(brand);
        menu.add(subtitle);

        addPage("Sản phẩm", () -> new ProductAdminPanel(accountService));
        addPage("Danh mục", () -> new CategoryPanel(accountService));
        addPage("Thương hiệu", () -> new BrandPanel(accountService));
        addPage("Thuộc tính", () -> new AttributeAdminPanel(accountService));
        addPage("Tài khoản", () -> new AccountAdminPanel(accountService));
        addPage("Nhật ký", () -> new AdjustLogPanel(accountService));
        addPage("Thống kê", () -> new StatisticsPanel());

        pages.setBackground(Theme.BG);
        JPanel root = new JPanel(new BorderLayout());
        root.add(menu, BorderLayout.WEST);
        root.add(pages, BorderLayout.CENTER);
        setContentPane(root);

        show("Sản phẩm");
    }

    private void addPage(String name, Supplier<JComponent> factory) {
        factories.put(name, factory);
        NavItem item = new NavItem(name, () -> show(name));
        items.put(name, item);
        menu.add(item);
    }

    private void show(String name) {
        if (built.add(name)) {
            pages.add(factories.get(name).get(), name);
        }
        cards.show(pages, name);
        items.forEach((n, item) -> item.setSelected(n.equals(name)));
    }
}