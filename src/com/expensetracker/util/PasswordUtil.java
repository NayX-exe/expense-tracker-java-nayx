package com.expensetracker.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // fallback: simple obfuscation
            return Integer.toHexString(password.hashCode());
        }
    }

    public static boolean verify(String password, String hash) {
        return hash(password).equals(hash);
    }
}