package com.simplefanc.voj.remoteJudge;

import com.simplefanc.voj.util.Constants;
import lombok.Data;

import java.util.Date;

@Data
public class SubmissionRemoteStatus {

    public Constants.Judge statusType;

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
