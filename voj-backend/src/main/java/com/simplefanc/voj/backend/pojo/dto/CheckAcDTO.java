package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: chenfan
 * @Date: 2021/1/17 19:13
 * @Description:
 */
@Data
public class CheckAcDTO {

    @NotBlank(message = "比赛记录id不能为空")
    private Long id;

    @NotBlank(message = "比赛id不能为空")
    private Long cid;

    @NotBlank(message = "是否确认不能为空")
    private Boolean checked;

}