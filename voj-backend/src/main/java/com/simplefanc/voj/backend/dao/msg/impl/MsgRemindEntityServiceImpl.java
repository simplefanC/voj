package com.simplefanc.voj.backend.dao.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.backend.mapper.MsgRemindMapper;
import com.simplefanc.voj.backend.pojo.vo.UserMsgVo;
import com.simplefanc.voj.backend.pojo.vo.UserUnreadMsgCountVo;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:36
 * @Description:
 */
@Service
public class MsgRemindEntityServiceImpl extends ServiceImpl<MsgRemindMapper, MsgRemind>
        implements MsgRemindEntityService {

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