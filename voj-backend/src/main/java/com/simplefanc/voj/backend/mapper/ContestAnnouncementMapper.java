package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.contest.ContestAnnouncement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContestAnnouncementMapper extends BaseMapper<ContestAnnouncement> {
}
