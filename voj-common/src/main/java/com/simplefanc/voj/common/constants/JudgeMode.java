package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author chenfan
 * @date 2022/4/18 16:24
 **/
@Getter
@AllArgsConstructor
public enum JudgeMode {

    DEFAULT("default"),

    SPJ("spj"),

    INTERACTIVE("interactive");

    private final String mode;

    public static JudgeMode getJudgeMode(String mode) {
        for (JudgeMode judgeMode : JudgeMode.values()) {
            if (judgeMode.getMode().equals(mode)) {
                return judgeMode;
            }
        }
        return null;
    }
}
