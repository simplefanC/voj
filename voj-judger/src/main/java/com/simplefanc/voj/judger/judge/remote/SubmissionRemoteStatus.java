package com.simplefanc.voj.judger.judge.remote;

import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeCase;
import lombok.Data;

import java.util.Date;
import java.util.List;

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

    public List<JudgeCase> judgeCaseList;

    public Date queryTime = new Date();

}
