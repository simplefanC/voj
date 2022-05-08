package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 17:55
 * @Description:
 */
@Data
public class RegisterTrainingDto {

    @NotBlank(message = "tid不能为空")
    private Long tid;

    @NotBlank(message = "password不能为空")
    private String password;

}