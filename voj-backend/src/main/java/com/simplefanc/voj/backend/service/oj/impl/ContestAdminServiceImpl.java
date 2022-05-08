package com.simplefanc.voj.backend.service.oj.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestPrintEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.backend.pojo.dto.CheckAcDto;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.oj.ContestAdminService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestPrint;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 19:40
 * @Description:
 */
@Service
public class ContestAdminServiceImpl implements ContestAdminService {

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Autowired
    private ContestPrintEntityService contestPrintEntityService;

    @Override
    public IPage<ContestRecord> getContestACInfo(Long cid, Integer currentPage, Integer limit) {

        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = UserSessionUtil.isRoot();

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("对不起，你无权查看！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        // 获取当前比赛的，状态为ac，未被校验的排在前面
        return contestRecordEntityService.getACInfo(currentPage, limit, ContestEnum.RECORD_AC.getCode(), cid,
                contest.getUid());

    }

    @Override
    public void checkContestAcInfo(CheckAcDto checkAcDto) {

        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(checkAcDto.getCid());

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = UserSessionUtil.isRoot();

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("对不起，你无权操作！");
        }

        boolean isOk = contestRecordEntityService
                .updateById(new ContestRecord().setChecked(checkAcDto.getChecked()).setId(checkAcDto.getId()));

        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }

    }

    @Override
    public IPage<ContestPrint> getContestPrint(Long cid, Integer currentPage, Integer limit) {

        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = UserSessionUtil.isRoot();

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("对不起，你无权查看！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        // 获取当前比赛的，未被确定的排在签名

        IPage<ContestPrint> contestPrintIPage = new Page<>(currentPage, limit);

        QueryWrapper<ContestPrint> contestPrintQueryWrapper = new QueryWrapper<>();
        contestPrintQueryWrapper.select("id", "cid", "username", "realname", "status", "gmt_create").eq("cid", cid)
                .orderByAsc("status").orderByDesc("gmt_create");

        return contestPrintEntityService.page(contestPrintIPage, contestPrintQueryWrapper);
    }

    @Override
    public void checkContestPrintStatus(Long id, Long cid) {

        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = UserSessionUtil.isRoot();

        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("对不起，你无权操作！");
        }

        boolean isOk = contestPrintEntityService.updateById(new ContestPrint().setId(id).setStatus(1));

        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }
    }

}