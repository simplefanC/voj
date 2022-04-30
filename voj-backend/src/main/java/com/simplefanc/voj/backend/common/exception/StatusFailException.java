package com.simplefanc.voj.backend.common.exception;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 10:27
 * @Description:
 */
public class StatusFailException extends RuntimeException {
    public StatusFailException() {
    }

    public StatusFailException(String message) {
        super(message);
    }

    public StatusFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatusFailException(Throwable cause) {
        super(cause);
    }

    public StatusFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}