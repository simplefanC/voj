package com.simplefanc.voj.backend.common.exception;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 10:29
 * @Description:
 */
public class StatusForbiddenException extends RuntimeException {

    public StatusForbiddenException() {
    }

    public StatusForbiddenException(String message) {
        super(message);
    }

    public StatusForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatusForbiddenException(Throwable cause) {
        super(cause);
    }

    public StatusForbiddenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}