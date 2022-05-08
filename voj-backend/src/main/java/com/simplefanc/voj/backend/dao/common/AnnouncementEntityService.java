package com.simplefanc.voj.backend.dao.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVo;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;

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
