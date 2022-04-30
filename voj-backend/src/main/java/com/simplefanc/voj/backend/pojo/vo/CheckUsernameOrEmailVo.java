package com.simplefanc.voj.backend.pojo.vo;

import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 18:15
 * @Description:
 */
@Data
public class CheckUsernameOrEmailVo {

    private Boolean email;

    private Boolean username;
}