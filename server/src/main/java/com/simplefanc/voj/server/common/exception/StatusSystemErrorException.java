package com.simplefanc.voj.server.common.exception;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:33
 * @Description:
 */
public class StatusSystemErrorException extends RuntimeException {

    public StatusSystemErrorException() {
    }

    public StatusSystemErrorException(String message) {
        super(message);
    }

    public StatusSystemErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatusSystemErrorException(Throwable cause) {
        super(cause);
    }

    public StatusSystemErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}