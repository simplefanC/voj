package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author chenfan
 * @date 2022/9/21
 **/
@Getter
@AllArgsConstructor
public enum JudgeCaseMode {

    DEFAULT("default"),

    ITERATE_UNTIL_WRONG("iterate_until_wrong");

    private final String mode;
}
