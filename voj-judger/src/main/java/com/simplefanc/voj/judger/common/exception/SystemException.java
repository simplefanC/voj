package com.simplefanc.voj.judger.common.exception;

import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2021/1/31 00:17
 * @Description:
 */
@Data
public class SystemException extends Exception {

    private String stdout;

    private String stderr;

    public SystemException(String message, String stdout, String stderr) {
        super(message + " " + stderr);
        this.stdout = stdout;
        this.stderr = stderr;
    }

}