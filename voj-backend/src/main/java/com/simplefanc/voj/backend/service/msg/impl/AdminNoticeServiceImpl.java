package com.simplefanc.voj.backend.service.msg.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.msg.AdminSysNoticeEntityService;
import com.simplefanc.voj.backend.dao.msg.UserSysNoticeEntityService;
import com.simplefanc.voj.backend.pojo.vo.AdminSysNoticeVo;
import com.simplefanc.voj.backend.service.msg.AdminNoticeService;
import com.simplefanc.voj.common.pojo.entity.msg.AdminSysNotice;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:19
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AdminNoticeServiceImpl implements AdminNoticeService {

    private final AdminSysNoticeEntityService adminSysNoticeEntityService;

    private final UserSysNoticeEntityService userSysNoticeEntityService;

    @Override
    public IPage<AdminSysNoticeVo> getSysNotice(Integer limit, Integer currentPage, String type) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 5;

        return adminSysNoticeEntityService.getSysNotice(limit, currentPage, type);
    }

    @Override
    public void addSysNotice(AdminSysNotice adminSysNotice) {

        boolean isOk = adminSysNoticeEntityService.saveOrUpdate(adminSysNotice);
        if (!isOk) {
            throw new StatusFailException("发布失败");
        }
    }

    @Override
    public void deleteSysNotice(Long id) {

        boolean isOk = adminSysNoticeEntityService.removeById(id);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }

    @Override
    public void updateSysNotice(AdminSysNotice adminSysNotice) {
        boolean isOk = adminSysNoticeEntityService.saveOrUpdate(adminSysNotice);
        if (!isOk) {
            throw new StatusFailException("更新失败");
        }
    }

    @Override
    @Async
    public void syncNoticeToNewRegisterBatchUser(List<String> uidList) {
        QueryWrapper<AdminSysNotice> adminSysNoticeQueryWrapper = new QueryWrapper<>();
        adminSysNoticeQueryWrapper.eq("type", "All").le("gmt_create", new Date()).eq("state", true);
        List<AdminSysNotice> adminSysNotices = adminSysNoticeEntityService.list(adminSysNoticeQueryWrapper);
        if (adminSysNotices.size() == 0) {
            return;
        }
        List<UserSysNotice> userSysNoticeList = new ArrayList<>();
        for (String uid : uidList) {
            for (AdminSysNotice adminSysNotice : adminSysNotices) {
                UserSysNotice userSysNotice = new UserSysNotice();
                userSysNotice.setType("Sys").setSysNoticeId(adminSysNotice.getId()).setRecipientId(uid);
                userSysNoticeList.add(userSysNotice);
            }
        }
        userSysNoticeEntityService.saveOrUpdateBatch(userSysNoticeList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void addSingleNoticeToUser(String adminId, String recipientId, String title, String content, String type) {
        AdminSysNotice adminSysNotice = new AdminSysNotice();
        adminSysNotice.setAdminId(adminId).setType("Single").setTitle(title).setContent(content).setState(true)
                .setRecipientId(recipientId);
        boolean isOk = adminSysNoticeEntityService.save(adminSysNotice);
        if (isOk) {
            UserSysNotice userSysNotice = new UserSysNotice();
            userSysNotice.setRecipientId(recipientId).setSysNoticeId(adminSysNotice.getId()).setType(type);
            userSysNoticeEntityService.save(userSysNotice);
        }
    }

}