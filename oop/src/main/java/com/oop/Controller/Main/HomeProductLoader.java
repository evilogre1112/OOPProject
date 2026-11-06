package com.oop.Controller.Main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oop.Controller.Common.ImageUtil;
import com.oop.Controller.Common.ProductCardData;
import com.oop.Model.GroupProduct;
import com.oop.Model.Image;
import com.oop.Service.ProductService;

/**
 * Lấy sản phẩm cho trang chủ / trang Shop từ ProductService. Gọi trong SwingWorker, không
 * gọi trên EDT.
 */
public final class HomeProductLoader {
    private HomeProductLoader() {
    }

    public static List<ProductCardData> load(int limit) throws SQLException {
        List<GroupProduct> groups = ProductService.search(null, null, null, null, null, "newest", 0, limit);
        return toCards(groups);
    }

    /** Đổi danh sách nhóm sản phẩm thành dữ liệu thẻ (giá thấp nhất + ảnh đầu tiên đã load sẵn). */
    public static List<ProductCardData> toCards(List<GroupProduct> groups) throws SQLException {
        List<ProductCardData> cards = new ArrayList<>();
        for (GroupProduct group : groups) {
            double price = ProductService.getMinPrice(group.getId());
            List<Image> images = ProductService.getImages(group.getId()); // đã sắp theo ordinalNum
            String path = images.isEmpty() ? null : images.get(0).getUrl();

            cards.add(new ProductCardData(group.getId(), group.getName(), price, ImageUtil.load(path)));
        }
        return cards;
    }
}
