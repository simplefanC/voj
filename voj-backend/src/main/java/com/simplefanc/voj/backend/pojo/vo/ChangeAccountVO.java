package com.simplefanc.voj.backend.pojo.vo;

import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 17:58
 * @Description:
 */
@Data
public class ChangeAccountVO {

    private Integer code;

    private String msg;

    private UserInfoVO userInfo;

}