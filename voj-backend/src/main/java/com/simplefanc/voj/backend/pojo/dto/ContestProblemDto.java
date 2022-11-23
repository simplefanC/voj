package com.simplefanc.voj.backend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:10
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestProblemDto {

    @NotBlank(message = "题目id不能为空")
    private Long pid;

    @NotBlank(message = "比赛id不能为空")
    private Long cid;

//    @NotBlank(message = "题目在比赛中的展示id不能为空")
//    private String displayId;

}