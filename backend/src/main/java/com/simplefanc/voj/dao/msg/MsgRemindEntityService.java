package com.simplefanc.voj.dao.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.msg.MsgRemind;
import com.simplefanc.voj.pojo.vo.UserMsgVo;
import com.simplefanc.voj.pojo.vo.UserUnreadMsgCountVo;


/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:32
 * @Description:
 */
public interface MsgRemindEntityService extends IService<MsgRemind> {

    UserUnreadMsgCountVo getUserUnreadMsgCount(String uid);

    IPage<UserMsgVo> getUserMsg(Page<UserMsgVo> page, String uid, String action);
}