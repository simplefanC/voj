package com.simplefanc.voj.backend.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    ROOT(1000L, "root"),
    ADMIN(1001L, "admin"),
    DEFAULT_USER(1002L, "default_user"),
    NO_SUBIMIT_USER(1003L, "no_subimit_user"),
    NO_DISCUSS_USER(1004L, "no_discuss_user"),
    MUTE_USER(1005L, "mute_user"),
    NO_SUBMIT_NO_DISCUSS_USER(1006L, "no_submit_no_discuss_user"),
    NO_SUBMIT_MUTE_USER(1007L, "no_submit_mute_user"),
    PROBLEM_ADMIN(1008L, "problem_admin");

    private final Long id;
    private final String name;
}
