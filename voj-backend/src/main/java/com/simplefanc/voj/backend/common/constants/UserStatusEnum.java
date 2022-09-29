package com.simplefanc.voj.backend.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author chenfan
 * @Date 2022/9/28
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {
    NORMAL(0),
    FORBID(1),
    APPLYING(2);

    private final Integer status;
}