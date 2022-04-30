package com.simplefanc.voj.backend.common.constants;

/**
 * @Description 训练题单的一些常量
 * @Since 2021/11/20
 */
public enum TrainingEnum {

    AUTH_PRIVATE("Private"),
    AUTH_PUBLIC("Public");

    private final String value;

    TrainingEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
