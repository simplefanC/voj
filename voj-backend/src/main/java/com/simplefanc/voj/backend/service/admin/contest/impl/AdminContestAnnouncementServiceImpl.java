package com.simplefanc.voj.backend.service.admin.contest.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.common.AnnouncementEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestAnnouncementEntityService;
import com.simplefanc.voj.backend.pojo.dto.AnnouncementDTO;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVO;
import com.simplefanc.voj.backend.service.admin.contest.AdminContestAnnouncementService;
import com.simplefanc.voj.common.pojo.entity.contest.ContestAnnouncement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:19
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AdminContestAnnouncementServiceImpl implements AdminContestAnnouncementService {

    private final AnnouncementEntityService announcementEntityService;

    private final ContestAnnouncementEntityService contestAnnouncementEntityService;

    @Override
    public IPage<AnnouncementVO> getAnnouncementList(Integer limit, Integer currentPage, Long cid) {

        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }
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
    public void addAnnouncement(AnnouncementDTO announcementDTO) {
        boolean saveAnnouncement = announcementEntityService.save(announcementDTO.getAnnouncement());
        boolean saveContestAnnouncement = contestAnnouncementEntityService.saveOrUpdate(new ContestAnnouncement()
                .setAid(announcementDTO.getAnnouncement().getId()).setCid(announcementDTO.getCid()));
        if (!saveAnnouncement || !saveContestAnnouncement) {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    public void updateAnnouncement(AnnouncementDTO announcementDTO) {
        boolean isOk = announcementEntityService.saveOrUpdate(announcementDTO.getAnnouncement());
        // 删除成功
        if (!isOk) {
            throw new StatusFailException("更新失败！");
        }
    }

}