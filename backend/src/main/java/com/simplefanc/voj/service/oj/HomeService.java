package com.simplefanc.voj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.pojo.vo.ACMRankVo;
import com.simplefanc.voj.pojo.vo.AnnouncementVo;
import com.simplefanc.voj.pojo.vo.ContestVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @Since 2020/12/29
     */
    List<ContestVo> getRecentContest();

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
    List<ACMRankVo> getRecentSevenACRank();

    /**
     * @MethodName getRecentOtherContest
     * @Params * @param null
     * @Description 获取最近其他OJ的比赛信息列表
     * @Return CommonResult
     * @Since 2020/1/15
     */
    List<HashMap<String, Object>> getRecentOtherContest();

    /**
     * @MethodName getCommonAnnouncement
     * @Params * @param null
     * @Description 获取主页公告列表
     * @Return CommonResult
     * @Since 2020/12/29
     */
    IPage<AnnouncementVo> getCommonAnnouncement(Integer limit, Integer currentPage);

    /**
     * @MethodName getWebConfig
     * @Params * @param null
     * @Description 获取网站的基础配置。例如名字，缩写名字等等。
     * @Return CommonResult
     * @Since 2020/12/29
     */
    Map<Object, Object> getWebConfig();
}