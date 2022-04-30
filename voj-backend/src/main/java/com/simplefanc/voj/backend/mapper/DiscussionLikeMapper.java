package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.discussion.DiscussionLike;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface DiscussionLikeMapper extends BaseMapper<DiscussionLike> {
}
