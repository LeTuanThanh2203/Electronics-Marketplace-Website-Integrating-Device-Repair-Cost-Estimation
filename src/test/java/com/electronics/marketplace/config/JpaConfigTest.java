package com.electronics.marketplace.config;

import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ghim hai hanh vi ma JpaConfig dua vao. Ca hai deu tung bi hieu sai:
 *
 * 1. Placeholder trong persistence.xml KHONG tu resolve khi khong co
 *    -DDB_URL: Hibernate 6.4.4 chi thay ${TEN_SYSTEM_PROPERTY}, khong ho tro
 *    ${env.TEN}. Neu ai "don gian hoa" bang cach bo JpaConfig, app se chet luc
 *    khoi dong vi khong co JDBC URL.
 * 2. Override map truyen vao createEntityManagerFactory thang duoc gia tri
 *    trong file, nen placeholder khong resolve van khong sao.
 *
 * Khong can database that: chi kiem tra EMF co dung duoc khong.
 */
class JpaConfigTest {

    /**
     * Cung mot loi "khong co URL" nhung moi connection provider bao mot kieu:
     * DriverManager nem HHH10001004, HikariCP nem "jdbcUrl is required".
     * Kiem tra theo y nghia, dung ghim vao mot provider.
     */
    private static final String NO_URL_PATTERN = "(?i)(HHH10001004|jdbcUrl is required|No JDBC URL)";

    @Test
    void xmlPlaceholderMotMinhKhongDu_thieuOverrideThiKhongDungDuocEmf() {
        Throwable error = assertThrows(Throwable.class,
                () -> Persistence.createEntityManagerFactory(JpaConfig.PU_NAME));

        assertTrue(describe(error).matches("(?s).*" + NO_URL_PATTERN + ".*"),
                "Ky vong loi vi thieu JDBC URL khi chi co placeholder ${DB_URL}. "
                        + "Neu loi khac xuat hien, co the Hibernate da doi cach resolve placeholder - "
                        + "kiem tra lai JpaConfig. Loi thuc te: " + describe(error));
    }

    @Test
    void overrideMapThangPlaceholder_nenPlaceholderKhongResolveVanChay() {
        Map<String, Object> overrides = new HashMap<>();
        // Cong 1: chac chan khong co gi lang nghe -> loi ket noi, khong phai loi thieu URL.
        overrides.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://127.0.0.1:1/khong_ton_tai");
        overrides.put("jakarta.persistence.jdbc.user", "u");
        overrides.put("jakarta.persistence.jdbc.password", "p");

        try {
            Persistence.createEntityManagerFactory(JpaConfig.PU_NAME, overrides);
            // Khong nem loi nao cung la ket qua chap nhan duoc: nghia la URL da duoc nap.
        } catch (Throwable error) {
            assertFalse(describe(error).matches("(?s).*" + NO_URL_PATTERN + ".*"),
                    "Override map phai thang placeholder trong XML, nhung van bao thieu URL: "
                            + describe(error));
        }
    }

    private static String describe(Throwable error) {
        StringBuilder message = new StringBuilder();
        for (Throwable t = error; t != null; t = t.getCause()) {
            message.append(t.getMessage()).append(" | ");
        }
        return message.toString();
    }
}
