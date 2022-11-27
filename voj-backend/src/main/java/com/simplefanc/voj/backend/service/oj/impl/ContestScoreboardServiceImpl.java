package com.simplefanc.voj.backend.service.oj.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.exception.StatusNotFoundException;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.backend.pojo.dto.ContestRankDTO;
import com.simplefanc.voj.backend.pojo.vo.ContestOutsideInfo;
import com.simplefanc.voj.backend.pojo.vo.ContestVO;
import com.simplefanc.voj.backend.service.oj.ContestACMRankService;
import com.simplefanc.voj.backend.service.oj.ContestOIRankService;
import com.simplefanc.voj.backend.service.oj.ContestScoreboardService;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 20:02
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class ContestScoreboardServiceImpl implements ContestScoreboardService {

    private final ContestEntityService contestEntityService;

    private final ContestProblemEntityService contestProblemEntityService;

    private final ContestValidator contestValidator;

    private final ContestACMRankService contestACMRankService;

    private final ContestOIRankService contestOIRankService;

    @Override
    public ContestOutsideInfo getContestOutsideInfo(Long cid) {

        ContestVO contestInfo = contestEntityService.getContestInfoById(cid);

        if (contestInfo == null) {
            throw new StatusNotFoundException("访问错误：该比赛不存在！");
        }

        if (!contestInfo.getOpenRank()) {
            throw new StatusForbiddenException("本场比赛未开启外榜，禁止访问外榜！");
        }

        // 获取本场比赛的状态
        if (contestInfo.getStatus().equals(ContestEnum.STATUS_SCHEDULED.getCode())) {
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

    @Override
    public IPage getContestOutsideScoreboard(ContestRankDTO contestRankDTO) {
        Long cid = contestRankDTO.getCid();
        List<String> concernedList = contestRankDTO.getConcernedList();
        Boolean removeStar = contestRankDTO.getRemoveStar();
        Boolean forceRefresh = contestRankDTO.getForceRefresh();
        Integer currentPage = contestRankDTO.getCurrentPage();
        Integer limit = contestRankDTO.getLimit();

        if (cid == null) {
            throw new StatusFailException("错误：比赛id不能为空");
        }
        if (removeStar == null) {
            removeStar = false;
        }
        if (forceRefresh == null) {
            forceRefresh = false;
        }
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 30;
        }

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("访问错误：该比赛不存在！");
        }

        if (!contest.getOpenRank()) {
            throw new StatusForbiddenException("本场比赛未开启外榜，禁止访问外榜！");
        }

        if (contest.getStatus().equals(ContestEnum.STATUS_SCHEDULED.getCode())) {
            throw new StatusForbiddenException("本场比赛正在筹备中，禁止访问外榜！");
        }

        // 不是比赛创建者或者超管无权限开启强制实时榜单
        if (!contestValidator.isContestAdmin(contest)) {
            forceRefresh = false;
        }

        // 校验该比赛是否开启了封榜模式，超级管理员和比赛创建者可以直接看到实际榜单
        boolean isOpenSealRank = contestValidator.isOpenSealRank(contest, forceRefresh);
        if (contest.getType().intValue() == ContestEnum.TYPE_ACM.getCode()) {
            // 获取ACM比赛排行榜外榜
            return contestACMRankService.getContestACMRankPage(contest, isOpenSealRank, removeStar, concernedList, contestRankDTO.getKeyword(),
                    !forceRefresh,
                    // 默认15s缓存
                    15L, currentPage, limit);

        } else {
            // 获取OI比赛排行榜外榜
            return contestOIRankService.getContestOIRankPage(contest, isOpenSealRank, removeStar, concernedList, contestRankDTO.getKeyword(),
                    !forceRefresh,
                    // 默认15s缓存
                    15L, currentPage, limit);
        }
    }

}