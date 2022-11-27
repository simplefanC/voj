package com.simplefanc.voj.backend.dao.discussion;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.DiscussionVO;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;

public interface DiscussionEntityService extends IService<Discussion> {

    DiscussionVO getDiscussion(Integer did, String uid);

    void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId);

}
