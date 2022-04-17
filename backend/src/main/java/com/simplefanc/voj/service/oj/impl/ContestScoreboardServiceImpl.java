package com.simplefanc.voj.service.oj.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.common.exception.StatusFailException;
import com.simplefanc.voj.common.exception.StatusForbiddenException;
import com.simplefanc.voj.common.exception.StatusNotFoundException;
import com.simplefanc.voj.dao.contest.ContestEntityService;
import com.simplefanc.voj.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.pojo.dto.ContestRankDto;
import com.simplefanc.voj.pojo.entity.contest.Contest;
import com.simplefanc.voj.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.pojo.vo.ContestOutsideInfo;
import com.simplefanc.voj.pojo.vo.ContestVo;
import com.simplefanc.voj.pojo.vo.UserRolesVo;
import com.simplefanc.voj.service.oj.ContestRankService;
import com.simplefanc.voj.service.oj.ContestScoreboardService;
import com.simplefanc.voj.utils.Constants;
import com.simplefanc.voj.validator.ContestValidator;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 20:02
 * @Description:
 */
@Service
public class ContestScoreboardServiceImpl implements ContestScoreboardService {

    @Resource
    private ContestEntityService contestEntityService;

    @Resource
    private ContestProblemEntityService contestProblemEntityService;

    @Resource
    private ContestValidator contestValidator;

    @Resource
    private ContestRankService contestRankService;

    public ContestOutsideInfo getContestOutsideInfo(Long cid) {

        ContestVo contestInfo = contestEntityService.getContestInfoById(cid);

        if (contestInfo == null) {
            throw new StatusNotFoundException("访问错误：该比赛不存在！");
        }

        if (!contestInfo.getOpenRank()) {
            throw new StatusForbiddenException("本场比赛未开启外榜，禁止访问外榜！");
        }

        // 获取本场比赛的状态
        if (contestInfo.getStatus().equals(Constants.Contest.STATUS_SCHEDULED.getCode())) {
            throw new StatusForbiddenException("本场比赛正在筹备中，禁止访问外榜！");
        }

        contestInfo.setNow(new Date());
        ContestOutsideInfo contestOutsideInfo = new ContestOutsideInfo();
        contestOutsideInfo.setContest(contestInfo);

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).orderByAsc("display_id");
        List<ContestProblem> contestProblemList = contestProblemEntityService.list(contestProblemQueryWrapper);
        contestOutsideInfo.setProblemList(contestProblemList);

        return contestOutsideInfo;
    }


    public List getContestOutsideScoreboard(ContestRankDto contestRankDto) {

        Long cid = contestRankDto.getCid();
        List<String> concernedList = contestRankDto.getConcernedList();
        Boolean removeStar = contestRankDto.getRemoveStar();
        Boolean forceRefresh = contestRankDto.getForceRefresh();

        if (cid == null) {
            throw new StatusFailException("错误：比赛id不能为空");
        }
        if (removeStar == null) {
            removeStar = false;
        }
        if (forceRefresh == null) {
            forceRefresh = false;
        }

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("访问错误：该比赛不存在！");
        }

        if (!contest.getOpenRank()) {
            throw new StatusForbiddenException("本场比赛未开启外榜，禁止访问外榜！");
        }

        if (contest.getStatus().equals(Constants.Contest.STATUS_SCHEDULED.getCode())) {
            throw new StatusForbiddenException("本场比赛正在筹备中，禁止访问外榜！");
        }

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = false;
        String currentUid = null;

        if (userRolesVo != null) {
            currentUid = userRolesVo.getUid();
            isRoot = SecurityUtils.getSubject().hasRole("root");
            // 不是比赛创建者或者超管无权限开启强制实时榜单
            if (!isRoot && !contest.getUid().equals(currentUid)) {
                forceRefresh = false;
            }
        }

        // 校验该比赛是否开启了封榜模式，超级管理员和比赛创建者可以直接看到实际榜单
        boolean isOpenSealRank = contestValidator.isSealRank(currentUid, contest, forceRefresh, isRoot);

        if (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) {

            // 获取ACM比赛排行榜外榜
            return contestRankService.getACMContestScoreboard(isOpenSealRank,
                    removeStar,
                    contest,
                    null,
                    concernedList,
                    !forceRefresh,
                    15L); // 默认15s缓存

        } else {
            // 获取OI比赛排行榜外榜
            return contestRankService.getOIContestScoreboard(isOpenSealRank,
                    removeStar,
                    contest,
                    null,
                    concernedList,
                    !forceRefresh,
                    15L); // 默认15s缓存
        }
    }
}