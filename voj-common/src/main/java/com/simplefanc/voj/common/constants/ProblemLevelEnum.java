package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProblemLevelEnum {
    PROBLEM_LEVEL_EASY(0, "Easy"),
    PROBLEM_LEVEL_MID(1, "Mid"),
    PROBLEM_LEVEL_HARD(2, "Hard");

    private final Integer code;
    private final String name;
}
