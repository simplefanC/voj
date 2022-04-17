package com.simplefanc.voj.service.admin.system.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.dao.contest.ContestEntityService;
import com.simplefanc.voj.dao.judge.JudgeEntityService;
import com.simplefanc.voj.dao.user.SessionEntityService;
import com.simplefanc.voj.dao.user.UserInfoEntityService;
import com.simplefanc.voj.pojo.entity.user.Session;
import com.simplefanc.voj.pojo.vo.UserRolesVo;
import com.simplefanc.voj.service.admin.system.DashboardService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:44
 * @Description:
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private SessionEntityService sessionEntityService;

    public Session getRecentSession() {
        // 需要获取一下该token对应用户的数据
        org.apache.shiro.session.Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<Session> wrapper = new QueryWrapper<Session>().eq("uid", userRolesVo.getUid()).orderByDesc("gmt_create");
        List<Session> sessionList = sessionEntityService.list(wrapper);
        if (sessionList.size() > 1) {
            return sessionList.get(1);
        } else {
            return sessionList.get(0);
        }
    }

    public Map<Object, Object> getDashboardInfo() {
        int userNum = userInfoEntityService.count();
        int recentContestNum = contestEntityService.getWithinNext14DaysContests().size();
        int todayJudgeNum = judgeEntityService.getTodayJudgeNum();
        return MapUtil.builder()
                .put("userNum", userNum)
                .put("recentContestNum", recentContestNum)
                .put("todayJudgeNum", todayJudgeNum).map();
    }
}