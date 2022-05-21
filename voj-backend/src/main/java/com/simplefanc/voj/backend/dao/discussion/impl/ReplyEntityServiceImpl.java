package com.simplefanc.voj.backend.dao.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.discussion.ReplyEntityService;
import com.simplefanc.voj.backend.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.backend.mapper.ReplyMapper;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/5/5 22:09
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class ReplyEntityServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements ReplyEntityService {

    private final MsgRemindEntityService msgRemindEntityService;

    @Async
    @Override
    public void updateReplyMsg(Integer sourceId, String sourceType, String content, Integer quoteId, String quoteType,
                               String recipientId, String senderId) {
        if (content.length() > 200) {
            content = content.substring(0, 200) + "...";
        }

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Reply").setSourceId(sourceId).setSourceType(sourceType).setSourceContent(content)
                .setQuoteId(quoteId).setQuoteType(quoteType).setUrl(sourceType.equals("Discussion")
                ? "/discussion-detail/" + sourceId : "/contest/" + sourceId + "/comment")
                .setRecipientId(recipientId).setSenderId(senderId);

        msgRemindEntityService.saveOrUpdate(msgRemind);
    }

}