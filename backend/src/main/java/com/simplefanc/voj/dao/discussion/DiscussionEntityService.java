package com.simplefanc.voj.dao.discussion;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.pojo.vo.DiscussionVo;

public interface DiscussionEntityService extends IService<Discussion> {

    DiscussionVo getDiscussion(Integer did, String uid);

    void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId);
}
