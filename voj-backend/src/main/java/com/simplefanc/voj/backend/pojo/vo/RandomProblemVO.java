package com.simplefanc.voj.backend.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 10:55
 * @Description:
 */
@Data
public class RandomProblemVO {

    @ApiModelProperty(value = "题目id")
    private String problemId;

}