package com.simplefanc.voj.backend.dao.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.discussion.DiscussionEntityService;
import com.simplefanc.voj.backend.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.backend.mapper.DiscussionMapper;
import com.simplefanc.voj.backend.pojo.vo.DiscussionVO;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/5/4 22:31
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class DiscussionEntityServiceImpl extends ServiceImpl<DiscussionMapper, Discussion>
        implements DiscussionEntityService {

    private final DiscussionMapper discussionMapper;

    private final MsgRemindEntityService msgRemindEntityService;

    @Override
    public DiscussionVO getDiscussion(Integer did, String uid) {
        return discussionMapper.getDiscussion(did, uid);
    }

    @Override
    @Async
    public void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId) {

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Like_Post").setRecipientId(recipientId).setSenderId(senderId).setSourceId(discussionId)
                .setSourceType("Discussion").setUrl("/discussion-detail/" + discussionId);
        msgRemindEntityService.saveOrUpdate(msgRemind);
    }

}