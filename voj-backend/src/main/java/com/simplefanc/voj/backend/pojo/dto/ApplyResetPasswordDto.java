package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 17:26
 * @Description:
 */
@Data
public class ApplyResetPasswordDto {

    private String captcha;

    private String captchaKey;

    private String email;

}