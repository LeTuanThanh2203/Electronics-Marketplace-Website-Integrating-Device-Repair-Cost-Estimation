package com.electronics.marketplace.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailUtilGmail {
    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = MailUtilGmail.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (in != null) CONFIG.load(in);
        } catch (IOException ignored) { }
    }

    // Ưu tiên biến môi trường, sau đó đến mail.properties
    private static String get(String env, String key) {
        String v = System.getenv(env);
        if (v != null && !v.isBlank()) return v.trim();
        String p = CONFIG.getProperty(key);
        return p == null ? "" : p.trim();
    }
    
     // Luồng riêng để gửi mail ngầm, không bắt người dùng chờ
    private static final ExecutorService MAIL_POOL = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "mail-sender");
        t.setDaemon(true);
        return t;
    });

    /** Gửi mail chào mừng ở luồng nền, trả về ngay. */
    public static void sendWelcomeAsync(String to, String name) {
        MAIL_POOL.submit(() -> {
            try {
                sendWelcome(to, name);
            } catch (MessagingException e) {
                System.err.println("Khong gui duoc mail toi " + to + ": " + e.getMessage());
            }
        });
    }

    public static void shutdown() {
        MAIL_POOL.shutdown();
    }

    private static String username() { return get("MAIL_USERNAME", "mail.username"); }
    private static String password() { return get("MAIL_PASSWORD", "mail.password"); }

    public static boolean isConfigured() { return !username().isEmpty() && !password().isEmpty(); }

    /** Tra DNS: tên miền của email có máy chủ nhận mail (MX) không. Lỗi mạng thì cho qua. */
    public static boolean domainHasMailServer(String email) {
        String domain = email.substring(email.lastIndexOf('@') + 1);
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            env.put("com.sun.jndi.dns.timeout.initial", "3000");
            env.put("com.sun.jndi.dns.timeout.retries", "1");
            DirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            ctx.close();
            Attribute mx = attrs.get("MX");
            return mx != null && mx.size() > 0;
        } catch (NameNotFoundException e) {
            return false;               // tên miền không tồn tại
        } catch (NamingException e) {
            return true;                // lỗi DNS/mạng: không kết luận được
        }
    }

    /** Gửi mail báo đăng ký thành công. Chưa cấu hình mail thì chỉ ghi log (chế độ dev). */
    public static void sendWelcome(String to, String name) throws MessagingException {
        if (!isConfigured()) {
            System.out.println("[DEV] Chua cau hinh mail, bo qua gui mail cho " + to);
            return;
        }
        String safeName = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String html = "<div style=\"font-family:Arial,sans-serif;max-width:480px\">"
            + "<h2 style=\"color:#2e7d32\">Chợ đồ cũ</h2>"
            + "<p>Xin chào <b>" + safeName + "</b>,</p>"
            + "<p>Bạn đã đăng ký tài khoản thành công. Chúc bạn mua bán vui vẻ!</p>"
            + "<p style=\"color:#6b7280;font-size:13px\">Nếu bạn không thực hiện đăng ký này, hãy bỏ qua email.</p>"
            + "</div>";
        sendMail(to, "Đăng ký tài khoản Chợ đồ cũ thành công", html, true);
    }

    /** Gửi mail qua Gmail (SMTPS cổng 465). Người gửi luôn là tài khoản Gmail đã cấu hình. */
    public static void sendMail(String to, String subject, String body, boolean bodyIsHTML)
            throws MessagingException {
        if (!isConfigured()) throw new MessagingException("Chua cau hinh mail.properties");

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", "smtp.gmail.com");
        props.put("mail.smtps.port", "465");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.ssl.enable", "true");
        props.put("mail.smtps.quitwait", "false");
        props.put("mail.smtps.connectiontimeout", "8000");
        props.put("mail.smtps.timeout", "8000");

        Session session = Session.getInstance(props);   // không bật debug để không in thông tin đăng nhập

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(username(), "Chợ đồ cũ", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Loi ma hoa ten nguoi gui", e);
        }
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, "UTF-8");               // để tiêu đề tiếng Việt không bị lỗi font
        if (bodyIsHTML) {
            message.setContent(body, "text/html; charset=UTF-8");
        } else {
            message.setText(body, "UTF-8");
        }

        try (Transport transport = session.getTransport("smtps")) {
            transport.connect("smtp.gmail.com", 465, username(), password());
            transport.sendMessage(message, message.getAllRecipients());
        }
    }
}
