package com.simplefanc.voj.backend.dao.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.backend.mapper.MsgRemindMapper;
import com.simplefanc.voj.backend.pojo.vo.UserMsgVO;
import com.simplefanc.voj.backend.pojo.vo.UserUnreadMsgCountVO;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:36
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class MsgRemindEntityServiceImpl extends ServiceImpl<MsgRemindMapper, MsgRemind>
        implements MsgRemindEntityService {

    private final MsgRemindMapper msgRemindMapper;

    @Override
    public UserUnreadMsgCountVO getUserUnreadMsgCount(String uid) {
        return msgRemindMapper.getUserUnreadMsgCount(uid);
    }

    @Override
    public IPage<UserMsgVO> getUserMsg(Page<UserMsgVO> page, String uid, String action) {
        return msgRemindMapper.getUserMsg(page, uid, action);
    }

}