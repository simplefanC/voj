package com.simplefanc.voj.backend.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.UserMsgVO;
import com.simplefanc.voj.backend.pojo.vo.UserUnreadMsgCountVO;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 10:36
 * @Description:
 */

public interface UserMessageService {

    UserUnreadMsgCountVO getUnreadMsgCount();

    void cleanMsg(String type, Long id);

    IPage<UserMsgVO> getCommentMsg(Integer limit, Integer currentPage);

    IPage<UserMsgVO> getReplyMsg(Integer limit, Integer currentPage);

    IPage<UserMsgVO> getLikeMsg(Integer limit, Integer currentPage);

    void updateUserMsgRead(IPage<UserMsgVO> userMsgList);

}