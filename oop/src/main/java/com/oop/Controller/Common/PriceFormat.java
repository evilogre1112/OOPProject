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

    /** Số tiền luôn hiện đầy đủ, kể cả 0 (dùng cho doanh thu). */
    public static String amount(double value) {
        return VND.format(value) + " đ";
    }

    /** Rút gọn để ghi trên biểu đồ: 1,2 tr / 350 k. */
    public static String compact(double value) {
        Locale vi = Locale.forLanguageTag("vi-VN");
        if (value >= 1e9) {
            return String.format(vi, "%.1f tỷ", value / 1e9);
        }
        if (value >= 1e6) {
            return String.format(vi, "%.1f tr", value / 1e6);
        }
        if (value >= 1e3) {
            return String.format(vi, "%.0f k", value / 1e3);
        }
        return String.format(vi, "%.0f", value);
    }

    public static String vnd(double price) {
        return price <= 0 ? "Liên hệ" : VND.format(price) + " đ";
    }
}
