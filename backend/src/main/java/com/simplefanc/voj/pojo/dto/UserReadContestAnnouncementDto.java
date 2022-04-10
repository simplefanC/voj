package com.simplefanc.voj.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/7/17 15:31
 * @Description:
 */

@Data
public class UserReadContestAnnouncementDto {

    private Long cid;

    private List<Long> readAnnouncementList;
}