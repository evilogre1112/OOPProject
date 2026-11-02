package com.oop.Controller.Common;

import java.text.NumberFormat;
import java.util.Locale;

/** Định dạng giá tiền kiểu Việt Nam: 460.000 đ. Chỉ dùng trên EDT. */
public final class PriceFormat {
    private static final NumberFormat VND = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    static {
        VND.setMaximumFractionDigits(0);
    }

    private PriceFormat() {
    }

    public static String vnd(double price) {
        return price <= 0 ? "Liên hệ" : VND.format(price) + " đ";
    }
}
