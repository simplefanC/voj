package com.simplefanc.voj.pojo.dto;

import lombok.Data;
import com.simplefanc.voj.pojo.entity.common.Announcement;

import javax.validation.constraints.NotBlank;

/**
 * @Author: chenfan
 * @Date: 2020/12/21 22:55
 * @Description:
 */
@Data
public class AnnouncementDto {
    @NotBlank(message = "比赛id不能为空")
    private Long cid;

    private Announcement announcement;
}