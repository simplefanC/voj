package com.simplefanc.voj.backend.service.admin.contest.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.pojo.entity.contest.ContestAnnouncement;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.common.AnnouncementEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestAnnouncementEntityService;
import com.simplefanc.voj.backend.pojo.dto.AnnouncementDto;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVo;
import com.simplefanc.voj.backend.service.admin.contest.AdminContestAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:19
 * @Description:
 */
@Service
public class AdminContestAnnouncementServiceImpl implements AdminContestAnnouncementService {

    @Autowired
    private AnnouncementEntityService announcementEntityService;

    @Autowired
    private ContestAnnouncementEntityService contestAnnouncementEntityService;

    @Override
    public IPage<AnnouncementVo> getAnnouncementList(Integer limit, Integer currentPage, Long cid) {

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        return announcementEntityService.getContestAnnouncement(cid, false, limit, currentPage);
    }

    @Override
    public void deleteAnnouncement(Long aid) {
        boolean isOk = announcementEntityService.removeById(aid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAnnouncement(AnnouncementDto announcementDto) {
        boolean saveAnnouncement = announcementEntityService.save(announcementDto.getAnnouncement());
        boolean saveContestAnnouncement = contestAnnouncementEntityService.saveOrUpdate(new ContestAnnouncement()
                .setAid(announcementDto.getAnnouncement().getId())
                .setCid(announcementDto.getCid()));
        if (!saveAnnouncement || !saveContestAnnouncement) {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    public void updateAnnouncement(AnnouncementDto announcementDto) {
        boolean isOk = announcementEntityService.saveOrUpdate(announcementDto.getAnnouncement());
        // 删除成功
        if (!isOk) {
            throw new StatusFailException("更新失败！");
        }
    }
}