package com.simplefanc.voj.backend.service.admin.announcement.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.common.AnnouncementEntityService;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVO;
import com.simplefanc.voj.backend.service.admin.announcement.AdminAnnouncementService;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:40
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private final AnnouncementEntityService announcementEntityService;

    @Override
    public IPage<AnnouncementVO> getAnnouncementList(Integer limit, Integer currentPage) {
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }
        return announcementEntityService.getAnnouncementList(limit, currentPage, false);

    }

    @Override
    public void deleteAnnouncement(long aid) {
        boolean isOk = announcementEntityService.removeById(aid);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }

    @Override
    public void addAnnouncement(Announcement announcement) {
        boolean isOk = announcementEntityService.save(announcement);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    public void updateAnnouncement(Announcement announcement) {
        boolean isOk = announcementEntityService.saveOrUpdate(announcement);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

}