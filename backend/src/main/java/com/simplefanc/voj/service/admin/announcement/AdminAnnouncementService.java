package com.simplefanc.voj.service.admin.announcement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.pojo.entity.common.Announcement;
import com.simplefanc.voj.pojo.vo.AnnouncementVo;


/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:40
 * @Description:
 */
public interface AdminAnnouncementService {
    IPage<AnnouncementVo> getAnnouncementList(Integer limit, Integer currentPage);

    void deleteAnnouncement(long aid);

    void addAnnouncement(Announcement announcement);

    void updateAnnouncement(Announcement announcement);
}