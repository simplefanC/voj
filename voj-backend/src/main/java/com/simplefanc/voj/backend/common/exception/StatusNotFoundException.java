package com.simplefanc.voj.backend.common.exception;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 10:30
 * @Description:
 */
public class StatusNotFoundException extends RuntimeException {

    public StatusNotFoundException() {
    }

    public StatusNotFoundException(String message) {
        super(message);
    }

    public StatusNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatusNotFoundException(Throwable cause) {
        super(cause);
    }

    public StatusNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}