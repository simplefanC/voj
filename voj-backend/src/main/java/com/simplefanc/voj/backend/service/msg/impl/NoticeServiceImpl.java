package com.simplefanc.voj.backend.service.msg.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.dao.msg.AdminSysNoticeEntityService;
import com.simplefanc.voj.backend.dao.msg.UserSysNoticeEntityService;
import com.simplefanc.voj.backend.pojo.vo.SysMsgVo;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.msg.NoticeService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.msg.AdminSysNotice;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 11:37
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final UserSysNoticeEntityService userSysNoticeEntityService;

    private final AdminSysNoticeEntityService adminSysNoticeEntityService;

    private final ApplicationContext applicationContext;

    @Override
    public IPage<SysMsgVo> getSysNotice(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 5;
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        IPage<SysMsgVo> sysNotice = userSysNoticeEntityService.getSysNotice(limit, currentPage, userRolesVo.getUid());
        applicationContext.getBean(NoticeService.class).updateSysOrMineMsgRead(sysNotice);
        return sysNotice;
    }

    @Override
    public IPage<SysMsgVo> getMineNotice(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 5;
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        IPage<SysMsgVo> mineNotice = userSysNoticeEntityService.getMineNotice(limit, currentPage, userRolesVo.getUid());
        applicationContext.getBean(NoticeService.class).updateSysOrMineMsgRead(mineNotice);
        return mineNotice;
    }

    @Override
    @Async
    public void updateSysOrMineMsgRead(IPage<SysMsgVo> userMsgList) {
        List<Long> idList = userMsgList.getRecords().stream().filter(userMsgVo -> !userMsgVo.getState())
                .map(SysMsgVo::getId).collect(Collectors.toList());
        if (idList.size() == 0) {
            return;
        }
        UpdateWrapper<UserSysNotice> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", idList).set("state", true);
        userSysNoticeEntityService.update(null, updateWrapper);
    }

    @Override
    @Async
    public void syncNoticeToNewRegisterUser(String uid) {
        QueryWrapper<AdminSysNotice> adminSysNoticeQueryWrapper = new QueryWrapper<>();
        adminSysNoticeQueryWrapper.eq("type", "All").le("gmt_create", new Date()).eq("state", true);
        List<AdminSysNotice> adminSysNotices = adminSysNoticeEntityService.list(adminSysNoticeQueryWrapper);
        if (adminSysNotices.size() == 0) {
            return;
        }
        List<UserSysNotice> userSysNoticeList = new ArrayList<>();
        for (AdminSysNotice adminSysNotice : adminSysNotices) {
            UserSysNotice userSysNotice = new UserSysNotice();
            userSysNotice.setType("Sys").setSysNoticeId(adminSysNotice.getId()).setRecipientId(uid);
            userSysNoticeList.add(userSysNotice);
        }
        userSysNoticeEntityService.saveOrUpdateBatch(userSysNoticeList);
    }

}