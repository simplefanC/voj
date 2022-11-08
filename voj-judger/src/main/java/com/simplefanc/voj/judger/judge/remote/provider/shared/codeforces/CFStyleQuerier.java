package com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces;

import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.judge.remote.querier.Querier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j(topic = "voj")
public abstract class CFStyleQuerier implements Querier {

    protected static final Map<String, JudgeStatus> STATUS_MAP = new HashMap<>() {
        {
            put("FAILED", JudgeStatus.STATUS_SUBMITTED_FAILED);
            put("OK", JudgeStatus.STATUS_ACCEPTED);
            put("PARTIAL", JudgeStatus.STATUS_PARTIAL_ACCEPTED);
            put("COMPILATION_ERROR", JudgeStatus.STATUS_COMPILE_ERROR);
            put("RUNTIME_ERROR", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("WRONG_ANSWER", JudgeStatus.STATUS_WRONG_ANSWER);
            put("PRESENTATION_ERROR", JudgeStatus.STATUS_PRESENTATION_ERROR);
            put("TIME_LIMIT_EXCEEDED", JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED);
            put("MEMORY_LIMIT_EXCEEDED", JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("IDLENESS_LIMIT_EXCEEDED", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("SECURITY_VIOLATED", JudgeStatus.STATUS_RUNTIME_ERROR);
            put("CRASHED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("INPUT_PREPARATION_CRASHED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("CHALLENGED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("SKIPPED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("TESTING", JudgeStatus.STATUS_JUDGING);
            put("REJECTED", JudgeStatus.STATUS_SYSTEM_ERROR);
            put("RUNNING & JUDGING", JudgeStatus.STATUS_JUDGING);
        }
    };

}
