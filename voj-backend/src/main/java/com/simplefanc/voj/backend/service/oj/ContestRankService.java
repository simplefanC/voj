package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.pojo.vo.ACMContestRankVo;
import com.simplefanc.voj.backend.pojo.vo.OIContestRankVo;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 20:30
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class ContestRankService {

    private final ContestCalculateAcmRankService contestCalculateAcmRankService;

    private final ContestCalculateOiRankService contestCalculateOiRankService;

    /**
     * @param isOpenSealRank
     * @param removeStar
     * @param concernedList
     * @param contest
     * @param currentPage
     * @param limit
     * @desc 获取ACM比赛排行榜，有分页
     */
    public IPage<ACMContestRankVo> getContestAcmRankPage(Boolean isOpenSealRank, Boolean removeStar,
                                                         List<String> concernedList, Contest contest, String keyword, int currentPage, int limit) {
        // 进行排序计算
        List<ACMContestRankVo> orderResultList = contestCalculateAcmRankService.calculateAcmRank(isOpenSealRank, removeStar,
                contest, concernedList, keyword);

        // 计算好排行榜，然后进行分页
        Page<ACMContestRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<ACMContestRankVo> pageList = new ArrayList<>();
        // 计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(orderResultList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);

        return page;
    }

    /**
     * @param isOpenSealRank
     * @param removeStarUser
     * @param concernedList
     * @param contest
     * @param currentPage
     * @param limit
     * @desc 获取OI比赛排行榜，有分页
     */
    public IPage<OIContestRankVo> getContestOiRankPage(Boolean isOpenSealRank, Boolean removeStarUser,
                                                       List<String> concernedList, Contest contest, String keyword, int currentPage, int limit) {

        List<OIContestRankVo> orderResultList = contestCalculateOiRankService.calculateOiRank(isOpenSealRank, removeStarUser,
                contest, concernedList, keyword);

        // 计算好排行榜，然后进行分页
        Page<OIContestRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<OIContestRankVo> pageList = new ArrayList<>();
        // 计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(orderResultList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);
        return page;
    }

    /**
     * @param isOpenSealRank
     * @param removeStar
     * @param contest
     * @param concernedList
     * @param useCache
     * @param cacheTime
     * @desc 获取ACM比赛排行榜外榜
     */
    public List<ACMContestRankVo> getAcmContestScoreboard(Boolean isOpenSealRank, Boolean removeStar, Contest contest,
                                                          List<String> concernedList, String keyword, Boolean useCache, Long cacheTime) {

        return contestCalculateAcmRankService.calculateAcmRank(isOpenSealRank, removeStar, contest,
                concernedList, keyword, useCache, cacheTime);
    }

    /**
     * @param isOpenSealRank
     * @param removeStar
     * @param contest
     * @param concernedList
     * @param useCache
     * @param cacheTime
     * @desc 获取OI比赛排行榜外榜
     */
    public List<OIContestRankVo> getOiContestScoreboard(Boolean isOpenSealRank, Boolean removeStar, Contest contest,
                                                        List<String> concernedList, String keyword, Boolean useCache, Long cacheTime) {

        return contestCalculateOiRankService.calculateOiRank(isOpenSealRank, removeStar, contest, concernedList, keyword,
                useCache, cacheTime);
    }

}