package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description 提交评测结果的状态码
 * @Since 2021/1/1
 */
@Getter
@AllArgsConstructor
public enum JudgeStatus {
    STATUS_NOT_SUBMITTED(-10, "Not Submitted", null),
    STATUS_SUBMITTED_UNKNOWN_RESULT(-5, "Submitted Unknown Result", null),
    STATUS_PRESENTATION_ERROR(-3, "Presentation Error", "pe"),
    STATUS_COMPILE_ERROR(-2, "Compile Error", "ce"),
    STATUS_WRONG_ANSWER(-1, "Wrong Answer", "wa"),
    STATUS_ACCEPTED(0, "Accepted", "ac"),
    STATUS_TIME_LIMIT_EXCEEDED(1, "Time Limit Exceeded", "tle"),
    STATUS_MEMORY_LIMIT_EXCEEDED(2, "Memory Limit Exceeded", "mle"),
    STATUS_RUNTIME_ERROR(3, "Runtime Error", "re"),
    STATUS_SYSTEM_ERROR(4, "System Error", "se"),
    STATUS_PENDING(5, "Pending", null),
    STATUS_COMPILING(6, "Compiling", null),
    STATUS_JUDGING(7, "Judging", null),
    STATUS_PARTIAL_ACCEPTED(8, "Partial Accepted", "pa"),
    STATUS_SUBMITTING(9, "Submitting", null),
    STATUS_SUBMITTED_FAILED(10, "Submitted Failed", null),
    STATUS_OUTPUT_LIMIT_EXCEEDED(11, "Output Limit Exceeded", null),
    STATUS_NULL(15, "No Status", null),
    JUDGE_SERVER_SUBMIT_PREFIX(-1002, "Judge SubmitId-ServerId:", null);

    private final Integer status;

    private final String name;

    private final String columnName;

}
