package com.simplefanc.voj.backend.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/1/3 16:50
 * @Description:
 */
@Data
public class SubmitIdListDTO {

    @NotEmpty(message = "查询的提交id列表不能为空")
    private List<Long> submitIds;

    private Long cid;

}