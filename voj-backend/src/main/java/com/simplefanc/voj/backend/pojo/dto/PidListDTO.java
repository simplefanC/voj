package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/12/28 22:33
 * @Description: 主要是获取前端题目列表页查询用户对应题目提交详情
 */
@Data
@Accessors(chain = true)
public class PidListDTO {

    @NotEmpty(message = "查询的题目id列表不能为空")
    private List<Long> pidList;

    @NotNull(message = "是否为比赛题目提交判断不能为空")
    private Boolean isContestProblemList;

    private Long cid;

}