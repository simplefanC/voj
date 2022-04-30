package com.simplefanc.voj.backend.common.exception;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 10:30
 * @Description:
 */
public class StatusAccessDeniedException extends RuntimeException {

    public StatusAccessDeniedException() {
    }

    public StatusAccessDeniedException(String message) {
        super(message);
    }

    public StatusAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatusAccessDeniedException(Throwable cause) {
        super(cause);
    }

    public StatusAccessDeniedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}