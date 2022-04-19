package com.simplefanc.voj.server.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.server.pojo.vo.UserMsgVo;
import com.simplefanc.voj.server.pojo.vo.UserUnreadMsgCountVo;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 10:36
 * @Description:
 */

public interface UserMessageService {

    UserUnreadMsgCountVo getUnreadMsgCount();

    void cleanMsg(String type, Long id);

    IPage<UserMsgVo> getCommentMsg(Integer limit, Integer currentPage);

    IPage<UserMsgVo> getReplyMsg(Integer limit, Integer currentPage);

    IPage<UserMsgVo> getLikeMsg(Integer limit, Integer currentPage);

    void updateUserMsgRead(IPage<UserMsgVo> userMsgList);
}