package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 17:26
 * @Description:
 */
@Data
public class ApplyResetPasswordDTO {

    @NotBlank(message = "邮箱或验证码不能为空")
    private String captcha;

    @NotBlank(message = "邮箱或验证码不能为空")
    private String captchaKey;

    @NotBlank(message = "邮箱或验证码不能为空")
    private String email;

}