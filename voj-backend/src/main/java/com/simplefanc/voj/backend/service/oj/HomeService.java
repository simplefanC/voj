package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.ACMRankVO;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVO;
import com.simplefanc.voj.backend.pojo.vo.ContestVO;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 21:00
 * @Description:
 */

public interface HomeService {

    /**
     * @MethodName getRecentContest
     * @Params * @param null
     * @Description 获取最近14天的比赛信息列表
     * @Return CommonResult
     * @Since 2021/12/29
     */
    List<ContestVO> getRecentContest();

    /**
     * @MethodName getHomeCarousel
     * @Params
     * @Description 获取主页轮播图
     * @Return
     * @Since 2021/9/4
     */
    List<HashMap<String, Object>> getHomeCarousel();

    /**
     * @MethodName getRecentSevenACRank
     * @Params * @param null
     * @Description 获取最近7天用户做题榜单
     * @Return
     * @Since 2021/1/15
     */
    List<ACMRankVO> getRecentSevenACRank();

    /**
     * @MethodName getRecentOtherContest
     * @Params * @param null
     * @Description 获取最近其他OJ的比赛信息列表
     * @Return CommonResult
     * @Since 2021/1/15
     */
    List<HashMap<String, Object>> getRecentOtherContest();

    /**
     * @MethodName getCommonAnnouncement
     * @Params * @param null
     * @Description 获取主页公告列表
     * @Return CommonResult
     * @Since 2021/12/29
     */
    IPage<AnnouncementVO> getCommonAnnouncement(Integer limit, Integer currentPage);

}