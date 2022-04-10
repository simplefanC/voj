package com.simplefanc.voj.dao.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.common.Announcement;
import com.simplefanc.voj.pojo.vo.AnnouncementVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
public interface AnnouncementEntityService extends IService<Announcement> {

    IPage<AnnouncementVo> getAnnouncementList(int limit, int currentPage, Boolean notAdmin);

    IPage<AnnouncementVo> getContestAnnouncement(Long cid, Boolean notAdmin, int limit, int currentPage);
}
