package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.simplefanc.voj.pojo.entity.discussion.DiscussionLike;


@Mapper
@Repository
public interface DiscussionLikeMapper extends BaseMapper<DiscussionLike> {
}
