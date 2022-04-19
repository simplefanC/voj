package com.simplefanc.voj.server.dao.discussion;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;

/**
 * @Author: chenfan
 * @Date: 2021/5/5 22:08
 * @Description:
 */
public interface ReplyEntityService extends IService<Reply> {

    public void updateReplyMsg(Integer sourceId, String sourceType, String content,
                               Integer quoteId, String quoteType, String recipientId, String senderId);
}