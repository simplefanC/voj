package com.simplefanc.voj.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.contest.ContestAnnouncementEntityService;
import com.simplefanc.voj.mapper.ContestAnnouncementMapper;
import com.simplefanc.voj.pojo.entity.contest.ContestAnnouncement;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2020/12/21 22:59
 * @Description:
 */
@Service
public class ContestAnnouncementEntityServiceImpl extends ServiceImpl<ContestAnnouncementMapper, ContestAnnouncement> implements ContestAnnouncementEntityService {
}