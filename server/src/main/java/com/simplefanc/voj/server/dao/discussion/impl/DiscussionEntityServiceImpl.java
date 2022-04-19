package com.simplefanc.voj.server.dao.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import com.simplefanc.voj.server.dao.discussion.DiscussionEntityService;
import com.simplefanc.voj.server.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.server.mapper.DiscussionMapper;
import com.simplefanc.voj.server.pojo.vo.DiscussionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2021/5/4 22:31
 * @Description:
 */
@Service
public class DiscussionEntityServiceImpl extends ServiceImpl<DiscussionMapper, Discussion> implements DiscussionEntityService {

    @Autowired
    private DiscussionMapper discussionMapper;
    @Resource
    private MsgRemindEntityService msgRemindEntityService;

    @Override
    public DiscussionVo getDiscussion(Integer did, String uid) {
        return discussionMapper.getDiscussion(did, uid);
    }

    @Override
    @Async
    public void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId) {

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Like_Post")
                .setRecipientId(recipientId)
                .setSenderId(senderId)
                .setSourceId(discussionId)
                .setSourceType("Discussion")
                .setUrl("/discussion-detail/" + discussionId);
        msgRemindEntityService.saveOrUpdate(msgRemind);
    }
}