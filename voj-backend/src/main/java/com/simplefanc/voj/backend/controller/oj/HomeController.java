package com.simplefanc.voj.backend.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.WebConfigDto;
import com.simplefanc.voj.backend.pojo.vo.ACMRankVo;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVo;
import com.simplefanc.voj.backend.pojo.vo.ContestVo;
import com.simplefanc.voj.backend.service.admin.system.ConfigService;
import com.simplefanc.voj.backend.service.oj.HomeService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/12/26 14:12
 * @Description: 处理首页的请求
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    private final ConfigService configService;

    /**
     * @MethodName getRecentContest
     * @Params * @param null
     * @Description 获取最近14天的比赛信息列表
     * @Return CommonResult
     * @Since 2021/12/29
     */
    @GetMapping("/get-recent-contest")
    public CommonResult<List<ContestVo>> getRecentContest() {
        return CommonResult.successResponse(homeService.getRecentContest());
    }

    /**
     * @MethodName getHomeCarousel
     * @Params
     * @Description 获取主页轮播图
     * @Return
     * @Since 2021/9/4
     */
    @GetMapping("/home-carousel")
    public CommonResult<List<HashMap<String, Object>>> getHomeCarousel() {
        return CommonResult.successResponse(homeService.getHomeCarousel());
    }

    /**
     * @MethodName getRecentSevenACRank
     * @Params * @param null
     * @Description 获取最近7天用户做题榜单
     * @Return
     * @Since 2021/1/15
     */
    @GetMapping("/get-recent-seven-ac-rank")
    public CommonResult<List<ACMRankVo>> getRecentSevenACRank() {
        return CommonResult.successResponse(homeService.getRecentSevenACRank());
    }

    /**
     * @MethodName getRecentOtherContest
     * @Params * @param null
     * @Description 获取最近其他OJ的比赛信息列表
     * @Return CommonResult
     * @Since 2021/1/15
     */
    @GetMapping("/get-recent-other-contest")
    public CommonResult<List<HashMap<String, Object>>> getRecentOtherContest() {
        return CommonResult.successResponse(homeService.getRecentOtherContest());
    }

    /**
     * @MethodName getCommonAnnouncement
     * @Params * @param null
     * @Description 获取主页公告列表
     * @Return CommonResult
     * @Since 2021/12/29
     */
    @GetMapping("/get-common-announcement")
    public CommonResult<IPage<AnnouncementVo>> getCommonAnnouncement(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return CommonResult.successResponse(homeService.getCommonAnnouncement(limit, currentPage));
    }

    /**
     * @MethodName getWebConfig
     * @Params * @param null
     * @Description 获取网站的基础配置。例如名字，缩写名字等等。
     * @Return CommonResult
     * @Since 2021/12/29
     */
    @GetMapping("/get-website-config")
    public CommonResult<WebConfigDto> getWebConfig() {
        return CommonResult.successResponse(configService.getWebConfig());
    }

}