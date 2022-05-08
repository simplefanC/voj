package com.simplefanc.voj.backend.dao.discussion;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.DiscussionVo;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;

public interface DiscussionEntityService extends IService<Discussion> {

    DiscussionVo getDiscussion(Integer did, String uid);

    void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId);

}
