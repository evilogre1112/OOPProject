package com.oop.Controller.Main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.oop.Controller.Common.LinkLabel;
import com.oop.Controller.Common.Theme;
import com.oop.Model.Account;

/** Thanh trên cùng: logo, menu, nút Admin (chỉ hiện với Admin), giỏ hàng / đơn hàng / địa chỉ / hồ sơ, lời chào + đăng xuất. */
public class LobbyHeader extends JPanel {
    private final LinkLabel cart;
    private final JLabel hello;

    public LobbyHeader(Account account, Runnable onLogout, Runnable onOpenAdmin,
            Runnable onShop, Runnable onHome, Runnable onCart, Runnable onOrders,
            Runnable onAddresses, Runnable onProfile) {
        super(new BorderLayout(32, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(20, 32, 20, 32)));

        JLabel brand = new JLabel("NAKED");
        brand.setFont(Theme.spaced(new Font("SansSerif", Font.PLAIN, 26), 0.4f));
        brand.setForeground(Theme.INK);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 28, 6));
        nav.setOpaque(false);
        nav.add(new LinkLabel("Shop Now", onShop));
        nav.add(new LinkLabel("New Arrivals", onHome));

        JPanel user = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 6));
        user.setOpaque(false);
        if ("Admin".equals(account.getRole())) {
            user.add(new LinkLabel("Admin", onOpenAdmin));
        }
        hello = new JLabel("Xin chào, " + account.getNickName());
        hello.setFont(Theme.BODY);
        hello.setForeground(Theme.MUTED);
        user.add(hello);

        cart = new LinkLabel("Giỏ hàng", onCart);
        user.add(cart);
        user.add(new LinkLabel("Đơn hàng", onOrders));
        user.add(new LinkLabel("Địa chỉ", onAddresses));
        user.add(new LinkLabel("Hồ sơ", onProfile));
        user.add(new LinkLabel("Đăng xuất", onLogout));

        add(brand, BorderLayout.WEST);
        add(nav, BorderLayout.CENTER);
        add(user, BorderLayout.EAST);
    }

    /** Ghi số dòng trong giỏ cạnh chữ "Giỏ hàng" (0 thì không ghi). */
    public void setCartCount(int count) {
        cart.setText(count > 0 ? "Giỏ hàng (" + count + ")" : "Giỏ hàng");
    }

    /** Cập nhật lời chào sau khi đổi nickname. */
    public void setNickName(String nickName) {
        hello.setText("Xin chào, " + nickName);
    }
}
