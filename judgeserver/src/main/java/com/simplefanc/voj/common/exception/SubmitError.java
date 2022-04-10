package com.simplefanc.voj.common.exception;

import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2021/4/16 13:52
 * @Description:
 */
@Data
public class SubmitError extends Exception {
    private String stdout;
    private String stderr;

    public SubmitError(String message, String stdout, String stderr) {
        super(message);
        this.stdout = stdout;
        this.stderr = stderr;
    }
}