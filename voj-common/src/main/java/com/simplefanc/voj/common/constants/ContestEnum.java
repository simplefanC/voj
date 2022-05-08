package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description 比赛相关的常量
 * @Since 2021/1/7
 */
@Getter
@AllArgsConstructor
public enum ContestEnum {
    TYPE_ACM(0, "ACM"),
    TYPE_OI(1, "OI"),

    STATUS_SCHEDULED(-1, "Scheduled"),
    STATUS_RUNNING(0, "Running"),
    STATUS_ENDED(1, "Ended"),

    AUTH_PUBLIC(0, "Public"),
    AUTH_PRIVATE(1, "Private"),
    AUTH_PROTECT(2, "Protect"),

    RECORD_NOT_AC_PENALTY(-1, "未AC通过算罚时"),
    RECORD_NOT_AC_NOT_PENALTY(0, "未AC通过不罚时"),
    RECORD_AC(1, "AC通过");

    private final Integer code;

    private final String name;
}
