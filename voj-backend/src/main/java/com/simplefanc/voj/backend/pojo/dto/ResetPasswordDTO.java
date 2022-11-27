package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 17:32
 * @Description:
 */

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Size(min = 6, max = 20, message = "新密码长度应该为6~20位！")
    @NotBlank(message = "新密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String code;

}