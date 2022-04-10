package com.simplefanc.voj.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:09
 * @Description:
 */
@Data
public class AdminEditUserDto {

    @NotBlank(message = "username不能为空")
    private String username;

    @NotBlank(message = "uid不能为空")
    private String uid;

    private String realname;

    private String email;

    private String password;

    private Integer type;

    private Integer status;

    private Boolean setNewPwd;
}