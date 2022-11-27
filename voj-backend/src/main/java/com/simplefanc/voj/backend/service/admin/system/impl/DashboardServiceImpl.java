package com.simplefanc.voj.backend.service.admin.system.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.user.SessionEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.service.admin.system.DashboardService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.user.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:44
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ContestEntityService contestEntityService;

    private final JudgeEntityService judgeEntityService;

    private final UserInfoEntityService userInfoEntityService;

    private final SessionEntityService sessionEntityService;

    @Override
    public Session getRecentSession() {
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        QueryWrapper<Session> wrapper = new QueryWrapper<Session>().eq("uid", userRolesVO.getUid())
                .orderByDesc("gmt_create");
        List<Session> sessionList = sessionEntityService.list(wrapper);
        if (sessionList.size() > 1) {
            return sessionList.get(1);
        } else {
            return sessionList.get(0);
        }
    }

    @Override
    public Map<Object, Object> getDashboardInfo() {
        int userNum = userInfoEntityService.count();
        int recentContestNum = contestEntityService.getWithinNext14DaysContests().size();
        int todayJudgeNum = judgeEntityService.getTodayJudgeNum();
        // TODO put 键
        return MapUtil.builder().put("userNum", userNum).put("recentContestNum", recentContestNum)
                .put("todayJudgeNum", todayJudgeNum).map();
    }

}