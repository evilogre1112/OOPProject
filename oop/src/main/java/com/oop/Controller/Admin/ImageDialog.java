package com.oop.Controller.Admin;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.oop.Controller.Common.Async;
import com.oop.Controller.Common.DataTable;
import com.oop.Controller.Common.Dialogs;
import com.oop.Controller.Common.FormDialog;
import com.oop.Controller.Common.SectionPanel;
import com.oop.Model.Image;
import com.oop.Model.Product;
import com.oop.Service.AccountService;
import com.oop.Service.ProductService;

/** Quản lý ảnh của một biến thể. Đường dẫn ảnh là đường dẫn tương đối từ thư mục gốc dự án. */
public class ImageDialog extends JDialog {
    private final AccountService accountService;
    private final Product variant;
    private final DataTable table = new DataTable("Tên", "Đường dẫn", "Thứ tự");
    private List<Image> images = new ArrayList<>();

    public ImageDialog(Component owner, AccountService accountService, Product variant) {
        super(SwingUtilities.getWindowAncestor(owner), "Ảnh của biến thể", ModalityType.APPLICATION_MODAL);
        this.accountService = accountService;
        this.variant = variant;

        SectionPanel section = new SectionPanel("Ảnh");
        section.bar().button("Thêm", this::onAdd);
        section.bar().button("Sửa", this::onEdit);
        section.bar().button("Xóa", this::onDelete);
        section.setBody(table);

        setContentPane(section);
        setSize(820, 480);
        setLocationRelativeTo(owner);
        reload();
    }

    private void reload() {
        Async.run(this, () -> ProductService.getImagesByProduct(variant.getId()), list -> {
            images = list;
            List<Object[]> rows = new ArrayList<>();
            for (Image img : list) {
                rows.add(new Object[] { img.getName(), img.getUrl(), img.getOrdinalNum() });
            }
            table.setRows(rows);
        });
    }

    private void onAdd() {
        String[] v = new FormDialog(this, "Thêm ảnh")
                .text("Tên ảnh (không trùng trong cùng biến thể)", "")
                .text("Đường dẫn (vd: images/products/Ao1.jpeg)", "images/products/")
                .text("Thứ tự hiển thị (1 = ảnh chính)", String.valueOf(images.size() + 1))
                .showDialog();
        if (v == null) {
            return;
        }
        Integer order = Dialogs.integer(this, v[2], "Thứ tự");
        if (order == null) {
            return;
        }
        Image image = new Image();
        image.setName(v[0]);
        image.setUrl(v[1]);
        image.setOrdinalNum(order);
        image.setProductId(variant.getId());
        Async.action(this, () -> ProductService.addImage(accountService, image), this::reload);
    }

    private void onEdit() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Image old = images.get(i);
        String[] v = new FormDialog(this, "Sửa ảnh \"" + old.getName() + "\"")
                .text("Đường dẫn", old.getUrl())
                .text("Thứ tự hiển thị", String.valueOf(old.getOrdinalNum()))
                .showDialog();
        if (v == null) {
            return;
        }
        Integer order = Dialogs.integer(this, v[1], "Thứ tự");
        if (order == null) {
            return;
        }
        Image image = new Image();
        image.setName(old.getName()); // tên là một phần khóa nên không đổi
        image.setUrl(v[0]);
        image.setOrdinalNum(order);
        image.setProductId(variant.getId());
        Async.action(this, () -> ProductService.updateImage(accountService, image), this::reload);
    }

    private void onDelete() {
        int i = table.requireSelection();
        if (i < 0) {
            return;
        }
        Image image = images.get(i);
        if (!Dialogs.confirm(this, "Xóa ảnh \"" + image.getName() + "\"?")) {
            return;
        }
        Async.action(this, () -> ProductService.deleteImage(accountService, variant.getId(), image.getName()), this::reload);
    }
}
