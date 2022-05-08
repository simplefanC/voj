package com.simplefanc.voj.backend.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author chenfan
 * @date 2022/5/7 22:41
 **/
@Getter
@AllArgsConstructor
public enum FileTypeEnum {
    AVATAR("avatar"),

    CAROUSEL("carousel"),

    MARKDOWN("md");

    private final String type;
}
