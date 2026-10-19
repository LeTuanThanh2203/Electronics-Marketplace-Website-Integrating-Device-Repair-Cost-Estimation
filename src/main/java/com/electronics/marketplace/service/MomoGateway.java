package com.electronics.marketplace.service;

import com.electronics.marketplace.config.MomoConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Goi REST API v2 cua MoMo bang JDK thuan (HttpURLConnection + javax.crypto).
 *
 * Vi sao khong dung SDK: momo-wallet/payment chi co sample C#/Go/Node/PHP/Ruby;
 * SDK Java rieng (io.github.momo-wallet:momopayment:1.0) keo log4j 1.2.17 da EOL.
 * Target Java 17 nen co java.net.http.HttpClient, nhung HttpURLConnection du dung
 * va khong can them code - giu nguyen.
 *
 * Khong co JSON library trong project: response cua MoMo la JSON phang, mot
 * extractor regex la du. KHONG dung cho JSON long nhau - xem ghi chu o field().
 */
public class MomoGateway {

    private static final int TIMEOUT_MS = 15000;

    private static final String CREATE_PATH = "/v2/gateway/api/create";
    private static final String QUERY_PATH = "/v2/gateway/api/query";

    private static final String PARTNER_NAME = "Sua Hay Ban";
    private static final String STORE_ID = "SHB";
    private static final String REQUEST_TYPE = "captureWallet";

    /** Ma loi MoMo tra ve khi orderId da duoc dung cho giao dich khac. */
    public static final int RESULT_DUPLICATE_ORDER_ID = 41;

    /** Query API: giao dich da khoi tao nhung chua co ket qua. KHONG phai loi. */
    public static final int RESULT_PENDING = 1000;

    /** Ket qua /v2/gateway/api/create. */
    public static class CreateResult {
        public final int resultCode;
        public final String message;
        public final String payUrl;

        CreateResult(int resultCode, String message, String payUrl) {
            this.resultCode = resultCode;
            this.message = message;
            this.payUrl = payUrl;
        }
    }

    /** Ket qua /v2/gateway/api/query. */
    public static class QueryResult {
        public final int resultCode;
        public final String message;
        public final long transId;
        public final long amount;

        QueryResult(int resultCode, String message, long transId, long amount) {
            this.resultCode = resultCode;
            this.message = message;
            this.transId = transId;
            this.amount = amount;
        }
    }

    public CreateResult create(String orderId, long amount, String orderInfo,
                               String redirectUrl, String ipnUrl, String extraData) throws IOException {
        MomoConfig cfg = MomoConfig.get();
        String requestId = orderId + "-" + System.nanoTime();

        // Thu tu field trong chuoi ky PHAI theo alphabet (yeu cau cua MoMo).
        String raw = "accessKey=" + cfg.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + cfg.getPartnerCode()
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + REQUEST_TYPE;
        String signature = hmacSha256(raw, cfg.getSecretKey());

        String body = "{"
                + "\"partnerCode\":\"" + esc(cfg.getPartnerCode()) + "\","
                + "\"partnerName\":\"" + esc(PARTNER_NAME) + "\","
                + "\"storeId\":\"" + esc(STORE_ID) + "\","
                + "\"requestId\":\"" + esc(requestId) + "\","
                + "\"amount\":" + amount + ","
                + "\"orderId\":\"" + esc(orderId) + "\","
                + "\"orderInfo\":\"" + esc(orderInfo) + "\","
                + "\"redirectUrl\":\"" + esc(redirectUrl) + "\","
                + "\"ipnUrl\":\"" + esc(ipnUrl) + "\","
                + "\"lang\":\"vi\","
                + "\"extraData\":\"" + esc(extraData) + "\","
                + "\"requestType\":\"" + REQUEST_TYPE + "\","
                + "\"signature\":\"" + signature + "\""
                + "}";

        String json = post(CREATE_PATH, body);
        return new CreateResult(
                intField(json, "resultCode"),
                field(json, "message"),
                field(json, "payUrl"));
    }

    public QueryResult query(String orderId) throws IOException {
        MomoConfig cfg = MomoConfig.get();
        String requestId = orderId + "-q-" + System.nanoTime();

        String raw = "accessKey=" + cfg.getAccessKey()
                + "&orderId=" + orderId
                + "&partnerCode=" + cfg.getPartnerCode()
                + "&requestId=" + requestId;
        String signature = hmacSha256(raw, cfg.getSecretKey());

        String body = "{"
                + "\"partnerCode\":\"" + esc(cfg.getPartnerCode()) + "\","
                + "\"requestId\":\"" + esc(requestId) + "\","
                + "\"orderId\":\"" + esc(orderId) + "\","
                + "\"signature\":\"" + signature + "\","
                + "\"lang\":\"vi\""
                + "}";

        String json = post(QUERY_PATH, body);
        return new QueryResult(
                intField(json, "resultCode"),
                field(json, "message"),
                longField(json, "transId"),
                longField(json, "amount"));
    }

    /**
     * IPN la form-encoded, MoMo ky tren chuoi field xep theo alphabet.
     * Thieu field nao thi MoMo gui chuoi rong, nen nz() la bat buoc.
     */
    public static boolean verifyIpn(Map<String, String> params) {
        MomoConfig cfg = MomoConfig.get();
        String raw = "accessKey=" + cfg.getAccessKey()
                + "&amount=" + nz(params.get("amount"))
                + "&extraData=" + nz(params.get("extraData"))
                + "&message=" + nz(params.get("message"))
                + "&orderId=" + nz(params.get("orderId"))
                + "&orderInfo=" + nz(params.get("orderInfo"))
                + "&orderType=" + nz(params.get("orderType"))
                + "&partnerCode=" + nz(params.get("partnerCode"))
                + "&payType=" + nz(params.get("payType"))
                + "&requestId=" + nz(params.get("requestId"))
                + "&responseTime=" + nz(params.get("responseTime"))
                + "&resultCode=" + nz(params.get("resultCode"))
                + "&transId=" + nz(params.get("transId"));
        String signature = params.get("signature");
        return signature != null
                && hmacSha256(raw, cfg.getSecretKey()).equalsIgnoreCase(signature.trim());
    }

    static String hmacSha256(String raw, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Khong tao duoc HMAC-SHA256", e);
        }
    }

    private static String post(String path, String body) throws IOException {
        HttpURLConnection con = (HttpURLConnection)
                new URL(MomoConfig.get().getEndpoint() + path).openConnection();
        try {
            con.setRequestMethod("POST");
            con.setConnectTimeout(TIMEOUT_MS);
            con.setReadTimeout(TIMEOUT_MS);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream out = con.getOutputStream()) {
                out.write(body.getBytes(StandardCharsets.UTF_8));
            }
            InputStream in = con.getResponseCode() >= 400
                    ? con.getErrorStream()
                    : con.getInputStream();
            return in == null ? "" : readFully(in);
        } finally {
            con.disconnect();
        }
    }

    private static String readFully(InputStream in) throws IOException {
        try (InputStream stream = in) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = stream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Response cua MoMo la JSON phang. Regex lay lan xuat hien DAU TIEN cua ten
     * field, du de doc resultCode/message/payUrl/transId/amount.
     * ponytail: khong phai JSON parser; gap response long nhau that su thi thay
     * bang thu vien JSON, khong viet tay them.
     */
    static String field(String json, String name) {
        if (json == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(name) + "\"\\s*:\\s*"
                        + "(?:\"((?:\\\\.|[^\"\\\\])*)\"|(-?[0-9]+(?:\\.[0-9]+)?))");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) != null ? unescape(matcher.group(1)) : matcher.group(2);
    }

    static int intField(String json, String name) {
        String value = field(json, name);
        try {
            return value == null ? -1 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static long longField(String json, String name) {
        String value = field(json, name);
        try {
            return value == null ? 0L : Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String unescape(String value) {
        if (value.indexOf('\\') < 0) {
            return value;
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) {
                out.append(c);
                continue;
            }
            char next = value.charAt(++i);
            switch (next) {
                case 'n': out.append('\n'); break;
                case 'r': out.append('\r'); break;
                case 't': out.append('\t'); break;
                case 'u':
                    if (i + 4 < value.length()) {
                        out.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    break;
                default: out.append(next);
            }
        }
        return out.toString();
    }

    private static String esc(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
