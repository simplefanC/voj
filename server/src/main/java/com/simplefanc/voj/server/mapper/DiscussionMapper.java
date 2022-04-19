package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.server.pojo.vo.DiscussionVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface DiscussionMapper extends BaseMapper<Discussion> {
    DiscussionVo getDiscussion(@Param("did") Integer did, @Param("uid") String uid);
}
