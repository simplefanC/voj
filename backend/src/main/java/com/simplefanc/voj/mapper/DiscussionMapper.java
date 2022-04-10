package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.simplefanc.voj.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.pojo.vo.DiscussionVo;


@Mapper
@Repository
public interface DiscussionMapper extends BaseMapper<Discussion> {
    DiscussionVo getDiscussion(@Param("did") Integer did, @Param("uid") String uid);
}
