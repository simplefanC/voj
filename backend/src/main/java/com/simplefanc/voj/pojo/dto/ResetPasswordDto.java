package com.simplefanc.voj.pojo.dto;

import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 17:32
 * @Description:
 */

@Data
public class ResetPasswordDto {

    private String username;

    private String password;

    private String code;
}