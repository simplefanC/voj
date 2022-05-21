package com.simplefanc.voj.backend.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestAnnouncementEntityService;
import com.simplefanc.voj.backend.mapper.ContestAnnouncementMapper;
import com.simplefanc.voj.common.pojo.entity.contest.ContestAnnouncement;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/12/21 22:59
 * @Description:
 */
@Service
public class ContestAnnouncementEntityServiceImpl extends ServiceImpl<ContestAnnouncementMapper, ContestAnnouncement>
        implements ContestAnnouncementEntityService {

}