package com.simplefanc.voj.dao.msg.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.mapper.MsgRemindMapper;
import com.simplefanc.voj.pojo.entity.msg.MsgRemind;
import com.simplefanc.voj.pojo.vo.UserMsgVo;
import com.simplefanc.voj.pojo.vo.UserUnreadMsgCountVo;

import javax.annotation.Resource;


/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:36
 * @Description:
 */
@Service
public class MsgRemindEntityServiceImpl extends ServiceImpl<MsgRemindMapper, MsgRemind> implements MsgRemindEntityService {

    @Resource
    private MsgRemindMapper msgRemindMapper;

    @Override
    public UserUnreadMsgCountVo getUserUnreadMsgCount(String uid) {
        return msgRemindMapper.getUserUnreadMsgCount(uid);
    }

    @Override
    public IPage<UserMsgVo> getUserMsg(Page<UserMsgVo> page, String uid, String action) {
        return msgRemindMapper.getUserMsg(page, uid, action);
    }

}