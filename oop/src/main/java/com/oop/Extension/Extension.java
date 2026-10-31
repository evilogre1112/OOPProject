package com.oop.Extension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Extension {
    /** Băm mật khẩu bằng SHA-256. */
    public  static String hash(String rawPassword) {
        if (rawPassword == null)
            return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hoá mật khẩu: ", e);
        }
    }
}
