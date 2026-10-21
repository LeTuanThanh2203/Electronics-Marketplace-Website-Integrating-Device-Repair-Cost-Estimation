package com.electronics.marketplace.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hash(String plain) { return BCrypt.hashpw(plain, BCrypt.gensalt(10)); }

    public static boolean verify(String plain, String hash) {
        try { return BCrypt.checkpw(plain, hash); } catch (Exception e) { return false; }
    }

    public static void main(String[] args) {
        System.out.println("admin123: " + hash("admin123"));
        System.out.println("user123: " + hash("user123"));
    }
}

