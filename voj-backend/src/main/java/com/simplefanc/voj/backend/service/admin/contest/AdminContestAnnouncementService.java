package com.simplefanc.voj.backend.service.admin.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.AnnouncementDTO;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVO;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:19
 * @Description:
 */

public interface AdminContestAnnouncementService {

    IPage<AnnouncementVO> getAnnouncementList(Integer limit, Integer currentPage, Long cid);

    void deleteAnnouncement(Long aid);

    void addAnnouncement(AnnouncementDTO announcementDTO);

    void updateAnnouncement(AnnouncementDTO announcementDTO);

}