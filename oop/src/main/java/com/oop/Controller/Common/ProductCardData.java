package com.oop.Controller.Common;

import java.awt.image.BufferedImage;

/** Dữ liệu nhẹ để vẽ một thẻ sản phẩm (đã load sẵn ảnh, không còn đụng tới DB). */
public class ProductCardData {
    public final String groupId;
    public final String name;
    public final double price;
    public final BufferedImage image; // có thể null nếu chưa có ảnh / sai đường dẫn

    public ProductCardData(String groupId, String name, double price, BufferedImage image) {
        this.groupId = groupId;
        this.name = name;
        this.price = price;
        this.image = image;
    }
}
