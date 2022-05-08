package com.simplefanc.voj.backend.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description 训练题单的一些常量
 * @Since 2021/11/20
 */
@Getter
@AllArgsConstructor
public enum TrainingEnum {

    AUTH_PRIVATE("Private"),
    AUTH_PUBLIC("Public");

    private final String value;
}
