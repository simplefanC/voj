package com.simplefanc.voj.backend.dao.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.UserMsgVO;
import com.simplefanc.voj.backend.pojo.vo.UserUnreadMsgCountVO;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;

/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:32
 * @Description:
 */
public interface MsgRemindEntityService extends IService<MsgRemind> {

    UserUnreadMsgCountVO getUserUnreadMsgCount(String uid);

    IPage<UserMsgVO> getUserMsg(Page<UserMsgVO> page, String uid, String action);

}