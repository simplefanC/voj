package com.simplefanc.voj.server.service.admin.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.server.pojo.dto.AnnouncementDto;
import com.simplefanc.voj.server.pojo.vo.AnnouncementVo;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:19
 * @Description:
 */

public interface AdminContestAnnouncementService {

    IPage<AnnouncementVo> getAnnouncementList(Integer limit, Integer currentPage, Long cid);

    void deleteAnnouncement(Long aid);

    void addAnnouncement(AnnouncementDto announcementDto);

    void updateAnnouncement(AnnouncementDto announcementDto);
}