package com.oop.Controller.Common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Đọc ảnh sản phẩm. Đường dẫn trong DB là đường dẫn tương đối tính từ thư mục
 * gốc dự án,
 * ví dụ "images/products/Ao1.jpeg" (dùng dấu /). Đường dẫn tuyệt đối vẫn dùng
 * được.
 * Gọi ngoài EDT (trong SwingWorker).
 */
public final class ImageUtil {
    private static final int MAX_SIDE = 1200; // ảnh lớn hơn sẽ được đọc thu nhỏ cho nhanh

    private ImageUtil() {
    }

    /** Trả về null nếu đường dẫn rỗng, không tìm thấy file hoặc không đọc được. */
    public static BufferedImage load(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        File file = find(path.trim().replace('\\', '/'));
        if (file == null) {
            System.err.println("Không tìm thấy ảnh: " + path + "\n  đã dò từ: " + startDirs());
            return null;
        }
        long start = System.currentTimeMillis();
        try {
            BufferedImage image = read(file);
            System.out.println("Đã tải " + file.getName() + " -> "
                    + (image == null ? "không đọc được" : image.getWidth() + "x" + image.getHeight())
                    + " (" + (System.currentTimeMillis() - start) + " ms)");
            return image;
        } catch (IOException e) {
            System.err.println("Không đọc được ảnh: " + file.getAbsolutePath() + " (" + e.getMessage() + ")");
            return null;
        }
    }

    /**
     * Đường dẫn tuyệt đối: dùng luôn. Tương đối: dò từ thư mục chạy và thư mục chứa
     * class, rồi đi dần lên các thư mục cha.
     */
    private static File find(String path) {
        File direct = new File(path);
        if (direct.isAbsolute()) {
            return direct.isFile() ? direct : null;
        }
        for (File dir : startDirs()) {
            for (File d = dir; d != null; d = d.getParentFile()) {
                File candidate = new File(d, path);
                if (candidate.isFile()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static java.util.List<File> startDirs() {
        java.util.List<File> dirs = new java.util.ArrayList<>();
        dirs.add(new File(System.getProperty("user.dir")));
        try {
            dirs.add(new File(ImageUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (Exception ignored) {
            // không lấy được vị trí class thì chỉ dùng user.dir
        }
        return dirs;
    }

    /** Đọc ảnh, bỏ bớt điểm ảnh khi ảnh quá lớn để giảm thời gian giải mã. */
    private static BufferedImage read(File file) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int step = Math.max(1, Math.max(reader.getWidth(0), reader.getHeight(0)) / MAX_SIDE);
                ImageReadParam param = reader.getDefaultReadParam();
                param.setSourceSubsampling(step, step, 0, 0);
                return reader.read(0, param);
            } finally {
                reader.dispose();
            }
        }
    }
}