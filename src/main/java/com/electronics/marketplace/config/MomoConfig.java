package com.electronics.marketplace.config;

/**
 * Cau hinh cong thanh toan MoMo (REST API v2).
 *
 * Doc tu bien moi truong, KHONG hardcode secret (project Rule 5):
 *   MOMO_PARTNER_CODE, MOMO_ACCESS_KEY, MOMO_SECRET_KEY  - bat buoc
 *   MOMO_ENDPOINT                                        - tuy chon, mac dinh sandbox
 *   MOMO_PUBLIC_BASE_URL                                 - tuy chon, vd https://abc.ngrok.app/shb
 *     Dia chi MoMo goi nguoc ve (redirectUrl/ipnUrl). Bo trong khi chay localhost:
 *     khi do lay tu request. IPN khong toi duoc localhost, phai dung tunnel/deploy.
 *
 * Vi sao goi thang REST thay vi dung SDK: momo-wallet/payment la sample code
 * (C#, Go, Node.js, PHP, Ruby), khong co Java. Goi Java SDK rieng
 * (io.github.momo-wallet:momopayment:1.0, 2019) keo theo log4j 1.2.17 da EOL.
 * Goi thang /v2/gateway/api/create + /query la cach duy nhat vua an toan vua
 * dung rang buoc "chi Servlet + JSP/JSTL + JPA chuan".
 *
 * Holder: chi doc bien moi truong o lan dau tien thuc su thanh toan MoMo, nen
 * app van khoi dong binh thuong khi chua cau hinh cong (COD khong can).
 */
public final class MomoConfig {

    private static final String ENV_PARTNER_CODE = "MOMO_PARTNER_CODE";
    private static final String ENV_ACCESS_KEY = "MOMO_ACCESS_KEY";
    private static final String ENV_SECRET_KEY = "MOMO_SECRET_KEY";
    private static final String ENV_ENDPOINT = "MOMO_ENDPOINT";
    private static final String ENV_PUBLIC_BASE_URL = "MOMO_PUBLIC_BASE_URL";

    private static final String DEFAULT_ENDPOINT = "https://test-payment.momo.vn";

    private final String partnerCode;
    private final String accessKey;
    private final String secretKey;
    private final String endpoint;
    private final String publicBaseUrl;

    private static class Holder {
        private static final MomoConfig INSTANCE = new MomoConfig();
    }

    private MomoConfig() {
        this.partnerCode = requireEnv(ENV_PARTNER_CODE);
        this.accessKey = requireEnv(ENV_ACCESS_KEY);
        this.secretKey = requireEnv(ENV_SECRET_KEY);
        String configured = System.getenv(ENV_ENDPOINT);
        this.endpoint = (configured == null || configured.trim().isEmpty())
                ? DEFAULT_ENDPOINT
                : stripTrailingSlash(configured.trim());
        String publicUrl = System.getenv(ENV_PUBLIC_BASE_URL);
        this.publicBaseUrl = (publicUrl == null || publicUrl.trim().isEmpty())
                ? null
                : stripTrailingSlash(publicUrl.trim());
    }

    public static MomoConfig get() {
        return Holder.INSTANCE;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    /** null = chua cau hinh, servlet tu dung scheme/host/port cua request. */
    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    /** Fail fast: thieu bien moi truong thi bao ngay, khong de loi HMAC kho hieu. */
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Thieu bien moi truong " + name + ". Dat MOMO_PARTNER_CODE, MOMO_ACCESS_KEY, MOMO_SECRET_KEY truoc khi thanh toan MoMo.");
        }
        return value.trim();
    }
}
