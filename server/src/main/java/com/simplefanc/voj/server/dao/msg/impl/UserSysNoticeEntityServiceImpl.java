package com.simplefanc.voj.server.dao.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;
import com.simplefanc.voj.server.dao.msg.UserSysNoticeEntityService;
import com.simplefanc.voj.server.mapper.UserSysNoticeMapper;
import com.simplefanc.voj.server.pojo.vo.SysMsgVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:35
 * @Description:
 */
@Service
public class UserSysNoticeEntityServiceImpl extends ServiceImpl<UserSysNoticeMapper, UserSysNotice> implements UserSysNoticeEntityService {

    @Resource
    private UserSysNoticeMapper userSysNoticeMapper;

    @Override
    public IPage<SysMsgVo> getSysNotice(int limit, int currentPage, String uid) {
        Page<SysMsgVo> page = new Page<>(currentPage, limit);
        return userSysNoticeMapper.getSysOrMineNotice(page, uid, "Sys");
    }

    @Override
    public IPage<SysMsgVo> getMineNotice(int limit, int currentPage, String uid) {
        Page<SysMsgVo> page = new Page<>(currentPage, limit);
        return userSysNoticeMapper.getSysOrMineNotice(page, uid, "Mine");
    }

}