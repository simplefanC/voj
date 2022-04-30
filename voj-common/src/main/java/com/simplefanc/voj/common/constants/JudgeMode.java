package com.simplefanc.voj.common.constants;

/**
 * @author chenfan
 * @date 2022/4/18 16:24
 **/
public enum JudgeMode {
    DEFAULT("default"),
    SPJ("spj"),
    INTERACTIVE("interactive");

    private final String mode;

    JudgeMode(String mode) {
        this.mode = mode;
    }

    public static JudgeMode getJudgeMode(String mode) {
        for (JudgeMode judgeMode : JudgeMode.values()) {
            if (judgeMode.getMode().equals(mode)) {
                return judgeMode;
            }
        }
        return null;
    }

    public String getMode() {
        return mode;
    }
}
