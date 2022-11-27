package com.simplefanc.voj.backend.service.admin.announcement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVO;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:40
 * @Description:
 */
public interface AdminAnnouncementService {

    IPage<AnnouncementVO> getAnnouncementList(Integer limit, Integer currentPage);

    void deleteAnnouncement(long aid);

    void addAnnouncement(Announcement announcement);

    void updateAnnouncement(Announcement announcement);

}