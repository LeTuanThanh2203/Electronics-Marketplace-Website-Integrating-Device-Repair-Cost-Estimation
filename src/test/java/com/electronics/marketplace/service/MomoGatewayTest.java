package com.electronics.marketplace.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Check cho phan khong goi duoc API that: chuoi ky HMAC va doc JSON phang.
 * Vector HMAC duoc doi chieu doc lap bang:
 * printf '%s' '&lt;raw&gt;' | openssl dgst -sha256 -hmac 'test-secret'
 *
 * Chay: mvn test -Dtest=MomoGatewayTest
 */
class MomoGatewayTest {

    private static final String CREATE_RAW = "accessKey=AK&amount=10000&extraData="
            + "&ipnUrl=http://localhost:8080/shb/payment?action=ipn"
            + "&orderId=TXN-abc-1&orderInfo=Thanh toan don hang DH-1&partnerCode=PC"
            + "&redirectUrl=http://localhost:8080/shb/payment?action=return"
            + "&requestId=REQ1&requestType=captureWallet";

    @Test
    void signsCreateRequestInAlphabeticalFieldOrder() {
        assertEquals("d5a3787abba93930dcacedb8dea899b7b03a810f5232f11a294f08b15cb0bbdd",
                MomoGateway.hmacSha256(CREATE_RAW, "test-secret"));
    }

    @Test
    void readsFlatJsonResponse() {
        String json = "{\"partnerCode\":\"PC\",\"orderId\":\"TXN-abc-1-1a2b3c4d\","
                + "\"amount\":10000,\"responseTime\":1690000000000,\"resultCode\":0,"
                + "\"message\":\"Successful.\","
                + "\"payUrl\":\"https://test-payment.momo.vn/v2/gateway/pay?t=abc\"}";

        assertEquals(0, MomoGateway.intField(json, "resultCode"));
        assertEquals(10000L, MomoGateway.longField(json, "amount"));
        assertEquals("TXN-abc-1-1a2b3c4d", MomoGateway.field(json, "orderId"));
        assertEquals("https://test-payment.momo.vn/v2/gateway/pay?t=abc",
                MomoGateway.field(json, "payUrl"));
    }

    @Test
    void reportsMissingFieldsInsteadOfThrowing() {
        String json = "{\"resultCode\":41,\"message\":\"Request rejected due to duplicated orderId.\"}";

        assertEquals(41, MomoGateway.intField(json, "resultCode"));
        assertNull(MomoGateway.field(json, "payUrl"));
        assertEquals(-1, MomoGateway.intField(json, "missing"));
        assertEquals(0L, MomoGateway.longField(json, "missing"));
    }
}
