package com.simplefanc.voj.backend.validator;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.dao.contest.ContestRegisterEntityService;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 20:06
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class ContestValidator {

    private final ContestRegisterEntityService contestRegisterEntityService;

    public boolean isContestAdmin(Contest contest) {
        // 超级管理员或比赛拥有者
        return UserSessionUtil.isRoot() || isContestOwner(contest.getUid());
    }

    public boolean isContestOwner(String uid) {
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();
        if(userRolesVO == null) {
            return false;
        }
        return uid.equals(userRolesVO.getUid());
    }

    public boolean isOpenSealRank(Contest contest, Boolean forceRefresh) {
        // 如果是管理员同时选择强制刷新榜单，则封榜无效
        if (forceRefresh && isContestAdmin(contest)) {
            return false;
        } else if (contest.getSealRank() && contest.getSealRankTime() != null) {
            // 该比赛开启封榜模式
            Date now = new Date();
            // 如果现在时间处于封榜开始到比赛结束之间
            if (now.after(contest.getSealRankTime()) && now.before(contest.getEndTime())) {
                return true;
            }
            // 或者没有开启赛后自动解除封榜，不可刷新榜单
            return !contest.getAutoRealRank() && now.after(contest.getEndTime());
        }
        return false;
    }

    public boolean checkVisible(Contest contest) {
        if (contest == null) {
            return false;
        }
        if (contest.getContestAdminVisible() && isContestAdmin(contest)) {
            return true;
        }
        return contest.getVisible();
    }

    /**
     * @param contest
     * @MethodName validateContestAuth
     * @Description 需要对该比赛做判断，是否处于开始或结束状态才可以获取，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
     * @Since 2021/1/17
     */
    public void validateContestAuth(Contest contest) {
        if (!checkVisible(contest)) {
            throw new StatusFailException("对不起，该比赛不存在！");
        }
        if (isContestAdmin(contest)) {
            return;
        }
        // 若不是比赛管理者
        // 判断一下比赛的状态，还未开始不能访问
        if (contest.getStatus().intValue() != ContestEnum.STATUS_RUNNING.getCode()
                && contest.getStatus().intValue() != ContestEnum.STATUS_ENDED.getCode()) {
            throw new StatusForbiddenException("比赛还未开始，您无权访问该比赛！");
        }

        // 如果是处于比赛正在进行阶段，需要判断该场比赛是否为私有赛，私有赛需要判断该用户是否已注册
        if (contest.getAuth().intValue() == ContestEnum.AUTH_PRIVATE.getCode()) {
            QueryWrapper<ContestRegister> registerQueryWrapper = new QueryWrapper<>();
            registerQueryWrapper.eq("cid", contest.getId()).eq("uid", UserSessionUtil.getUserInfo().getUid());
            ContestRegister register = contestRegisterEntityService.getOne(registerQueryWrapper);
            // 如果数据为空，表示未注册私有赛，不可访问
            if (register == null) {
                throw new StatusForbiddenException("对不起，请先到比赛首页输入比赛密码进行注册！");
            }

            if (contest.getOpenAccountLimit()
                    && !validateAccountRule(contest.getAccountLimitRule(), UserSessionUtil.getUserInfo().getUsername())) {
                throw new StatusForbiddenException("对不起！本次比赛只允许特定账号规则的用户参赛！");
            }
        }
    }

    public void validateJudgeAuth(Contest contest) {
        String uid = UserSessionUtil.getUserInfo().getUid();
        if (contest.getAuth().intValue() == ContestEnum.AUTH_PRIVATE.getCode()
                || contest.getAuth().intValue() == ContestEnum.AUTH_PROTECT.getCode()) {
            QueryWrapper<ContestRegister> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cid", contest.getId()).eq("uid", uid);
            ContestRegister register = contestRegisterEntityService.getOne(queryWrapper, false);
            // 如果还没注册
            if (register == null) {
                throw new StatusForbiddenException("对不起，请你先注册该比赛，提交代码失败！");
            }
        }
    }

    public boolean validateAccountRule(String accountRule, String username) {

        String prefix = ReUtil.get("<prefix>([\\s\\S]*?)</prefix>", accountRule, 1);
        String suffix = ReUtil.get("<suffix>([\\s\\S]*?)</suffix>", accountRule, 1);
        String start = ReUtil.get("<start>([\\s\\S]*?)</start>", accountRule, 1);
        String end = ReUtil.get("<end>([\\s\\S]*?)</end>", accountRule, 1);
        String extra = ReUtil.get("<extra>([\\s\\S]*?)</extra>", accountRule, 1);

        int startNum = Integer.parseInt(start);
        int endNum = Integer.parseInt(end);

        String formatString = "%0" + String.valueOf(endNum).length() + "d";
        for (int i = startNum; i <= endNum; i++) {
            String paddedNum = String.format(formatString, i);
            if (username.equals(prefix + paddedNum + suffix)) {
                return true;
            }
        }
        // 额外账号列表
        if (StrUtil.isNotEmpty(extra)) {
            String[] accountList = extra.trim().split("\n");
            for (String account : accountList) {
                if (username.equals(account)) {
                    return true;
                }
            }
        }

        return false;
    }

}