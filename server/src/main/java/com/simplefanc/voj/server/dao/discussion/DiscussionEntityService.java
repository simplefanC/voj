package com.simplefanc.voj.server.dao.discussion;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.server.pojo.vo.DiscussionVo;

public interface DiscussionEntityService extends IService<Discussion> {

    DiscussionVo getDiscussion(Integer did, String uid);

    void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId);
}
