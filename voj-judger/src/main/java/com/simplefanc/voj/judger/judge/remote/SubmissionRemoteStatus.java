package com.simplefanc.voj.judger.judge.remote;


import com.simplefanc.voj.common.constants.JudgeStatus;
import lombok.Data;

import java.util.Date;

@Data
public class SubmissionRemoteStatus {

    public JudgeStatus statusType;

    public String rawStatus;

    /**
     * millisecond
     */
    public int executionTime;

    /**
     * KiloBytes
     */
    public int executionMemory;

    public String compilationErrorInfo;

    public int failCase = -1;

    public Date queryTime = new Date();

}
