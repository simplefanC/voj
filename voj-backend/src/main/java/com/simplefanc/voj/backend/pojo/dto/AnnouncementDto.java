package com.simplefanc.voj.backend.pojo.dto;

import com.simplefanc.voj.common.pojo.entity.common.Announcement;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: chenfan
 * @Date: 2021/12/21 22:55
 * @Description:
 */
@Data
public class AnnouncementDto {

    @NotBlank(message = "比赛id不能为空")
    private Long cid;

    private Announcement announcement;

}