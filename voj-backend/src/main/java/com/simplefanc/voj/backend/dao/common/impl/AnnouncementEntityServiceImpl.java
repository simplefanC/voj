package com.simplefanc.voj.backend.dao.common.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.common.AnnouncementEntityService;
import com.simplefanc.voj.backend.mapper.AnnouncementMapper;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVo;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Service
public class AnnouncementEntityServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementEntityService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public IPage<AnnouncementVo> getAnnouncementList(int limit, int currentPage, Boolean notAdmin) {
        // 新建分页
        Page<AnnouncementVo> page = new Page<>(currentPage, limit);
        return announcementMapper.getAnnouncementList(page, notAdmin);
    }

    @Override
    public IPage<AnnouncementVo> getContestAnnouncement(Long cid, Boolean notAdmin, int limit, int currentPage) {
        Page<AnnouncementVo> page = new Page<>(currentPage, limit);
        return announcementMapper.getContestAnnouncement(page, cid, notAdmin);
    }

}
