package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProblemEnum {
    AUTH_PUBLIC(1, "Public"),
    AUTH_PRIVATE(2, "Private"),
    AUTH_CONTEST(3, "Contest");

    private final Integer code;
    private final String name;
}
