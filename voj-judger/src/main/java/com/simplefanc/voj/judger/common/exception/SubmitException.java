package com.simplefanc.voj.judger.common.exception;

import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2021/4/16 13:52
 * @Description:
 */
@Data
public class SubmitException extends Exception {

    private String stdout;

    private String stderr;

    public SubmitException(String message, String stdout, String stderr) {
        super(message);
        this.stdout = stdout;
        this.stderr = stderr;
    }

}