package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @Author: chenfan
 * @Date: 2021/10/24 11:15
 * @Description: 注册数据实体类
 */
@Data
@Accessors(chain = true)
public class RegisterDTO implements Serializable {

    @Nullable
    private String uuid;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名长度不能超过20位!")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    private String realname;

    @NotBlank(message = "学校不能为空")
    private String school;

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^[0-9A-Za-z]{6,18}$", message = "学号格式错误")
    private String number;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应该为6~20位！")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;

}