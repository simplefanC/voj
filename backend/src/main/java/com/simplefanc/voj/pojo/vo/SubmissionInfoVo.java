package com.simplefanc.voj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.simplefanc.voj.pojo.entity.judge.Judge;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 11:38
 * @Description:
 */
@Data
public class SubmissionInfoVo {

    @ApiModelProperty(value = "提交详情")
    private Judge submission;

    @ApiModelProperty(value = "提交者是否可以分享该代码")
    private Boolean codeShare;
}