package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.backend.pojo.vo.DiscussionVO;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DiscussionMapper extends BaseMapper<Discussion> {

    DiscussionVO getDiscussion(@Param("did") Integer did, @Param("uid") String uid);

}
