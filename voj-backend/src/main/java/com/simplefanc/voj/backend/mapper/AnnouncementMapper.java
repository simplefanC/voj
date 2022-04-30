package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    IPage<AnnouncementVo> getAnnouncementList(Page<AnnouncementVo> page, @Param("notAdmin") Boolean notAdmin);

    IPage<AnnouncementVo> getContestAnnouncement(Page<AnnouncementVo> page, @Param("cid") Long cid, @Param("notAdmin") Boolean notAdmin);
}
