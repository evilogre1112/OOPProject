package com.oop.Controller.Main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oop.Controller.Common.ImageUtil;
import com.oop.Controller.Common.ProductCardData;
import com.oop.DAO.ProductDAO;
import com.oop.Model.GroupProduct;
import com.oop.Service.ProductService;

/**
 * Lấy sản phẩm cho trang chủ / trang Shop từ ProductService. Gọi trong SwingWorker, không
 * gọi trên EDT.
 */
public final class HomeProductLoader {
    private HomeProductLoader() {
    }

    public static List<ProductCardData> load(int limit) throws SQLException {
        List<ProductDAO.CardSummary> summaries = ProductService.getNewestCardSummaries(limit);
        List<ProductCardData> cards = new ArrayList<>(summaries.size());
        for (ProductDAO.CardSummary item : summaries) {
            cards.add(new ProductCardData(item.groupId, item.name, item.minPrice, ImageUtil.load(item.imageUrl)));
        }
        return cards;
    }

    /** Đổi danh sách nhóm sản phẩm thành dữ liệu thẻ (giá thấp nhất + ảnh đầu tiên đã load sẵn). */
    public static List<ProductCardData> toCards(List<GroupProduct> groups) throws SQLException {
        List<ProductCardData> cards = new ArrayList<>(groups.size());
        if (groups.isEmpty()) {
            return cards;
        }
        List<String> groupIds = new ArrayList<>(groups.size());
        for (GroupProduct group : groups) {
            groupIds.add(group.getId());
        }
        Map<String, ProductDAO.CardSummary> summaries = ProductService.getCardSummaries(groupIds);
        for (GroupProduct group : groups) {
            ProductDAO.CardSummary item = summaries.get(group.getId());
            double price = item != null ? item.minPrice : 0.0;
            String path = item != null ? item.imageUrl : null;
            cards.add(new ProductCardData(group.getId(), group.getName(), price, ImageUtil.load(path)));
        }
        return cards;
    }
}
