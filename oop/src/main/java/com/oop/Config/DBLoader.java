package com.oop.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DBLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Không tìm thấy file config.properties!");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }
}
