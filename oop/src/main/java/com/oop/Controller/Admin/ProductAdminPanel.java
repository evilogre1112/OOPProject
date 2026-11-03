package com.oop.Controller.Admin;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.oop.Service.AccountService;

/** Trang Sản phẩm: bảng nhóm sản phẩm ở trên, bảng biến thể của nhóm đang chọn ở dưới. */
public class ProductAdminPanel extends JPanel {
    public ProductAdminPanel(AccountService accountService) {
        super(new BorderLayout());

        VariantPanel variants = new VariantPanel(accountService);
        GroupPanel groups = new GroupPanel(accountService, variants::showGroup);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, groups, variants);
        split.setBorder(null);
        split.setResizeWeight(0.5);
        split.setDividerLocation(390);
        add(split, BorderLayout.CENTER);
    }
}
