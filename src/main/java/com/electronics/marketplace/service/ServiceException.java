package com.electronics.marketplace.service;

/**
 * Loi nghiep vu can tra ve dung HTTP status (SDD 14: 403 sai chu so huu,
 * 409 xung dot trang thai, 400 du lieu sai). Servlet bat exception nay de
 * khong phai tu doan status tu message.
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int status;
    private final String code;

    public ServiceException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
