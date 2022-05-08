package com.simplefanc.voj.backend.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:37
 * @Description:
 */
@Data
public class CaptchaVo {

    @ApiModelProperty(value = "验证码图片的base64")
    private String img;

    @ApiModelProperty(value = "验证码key")
    private String captchaKey;

}