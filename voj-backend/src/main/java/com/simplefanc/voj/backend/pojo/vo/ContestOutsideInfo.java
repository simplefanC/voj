package com.simplefanc.voj.backend.pojo.vo;

import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/12/8 12:32
 * @Description:
 */
@ApiModel(value = "赛外排行榜所需的比赛信息，同时包括题目题号、气球颜色", description = "")
@Data
public class ContestOutsideInfo {

    @ApiModelProperty(value = "比赛信息")
    private ContestVo contest;

    @ApiModelProperty(value = "比赛题目信息列表")
    private List<ContestProblem> problemList;
}