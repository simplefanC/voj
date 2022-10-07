package com.simplefanc.voj.backend.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.constants.ScheduleConstant;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.common.AnnouncementEntityService;
import com.simplefanc.voj.backend.dao.common.FileEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.service.admin.user.UserRecordService;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.pojo.vo.ACMRankVo;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVo;
import com.simplefanc.voj.backend.pojo.vo.ContestVo;
import com.simplefanc.voj.backend.service.oj.HomeService;
import com.simplefanc.voj.common.pojo.entity.common.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 21:00
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final ContestEntityService contestEntityService;

    private final AnnouncementEntityService announcementEntityService;

    private final UserRecordService userRecordService;

    private final RedisUtil redisUtil;

    private final FileEntityService fileEntityService;

    private final FilePathProperties filePathProps;

    /**
     * @MethodName getRecentContest
     * @Params * @param null
     * @Description 获取最近14天的比赛信息列表
     * @Return CommonResult
     * @Since 2021/12/29
     */
    @Override
    public List<ContestVo> getRecentContest() {
        return contestEntityService.getWithinNext14DaysContests();
    }

    /**
     * @MethodName getHomeCarousel
     * @Params
     * @Description 获取主页轮播图
     * @Return
     * @Since 2021/9/4
     */
    @Override
    public List<HashMap<String, Object>> getHomeCarousel() {
        List<File> fileList = fileEntityService.queryCarouselFileList();
        List<HashMap<String, Object>> apiList = fileList.stream().map(f -> {
            // TODO put 键
            HashMap<String, Object> param = new HashMap<>(2);
            param.put("id", f.getId());
            param.put("url", filePathProps.getImgApi() + f.getName());
            return param;
        }).collect(Collectors.toList());
        return apiList;
    }

    /**
     * @MethodName getRecentSevenACRank
     * @Params * @param null
     * @Description 获取最近7天用户做题榜单
     * @Return
     * @Since 2021/1/15
     */
    @Override
    public List<ACMRankVo> getRecentSevenACRank() {
        return userRecordService.getRecent7ACRank();
    }

    /**
     * @MethodName getRecentOtherContest
     * @Params * @param null
     * @Description 获取最近其他OJ的比赛信息列表
     * @Return CommonResult
     * @Since 2021/1/15
     */
    @Override
    public List<HashMap<String, Object>> getRecentOtherContest() {
        String redisKey = ScheduleConstant.RECENT_OTHER_CONTEST;
        // 从redis获取比赛列表
        return (ArrayList<HashMap<String, Object>>) redisUtil.get(redisKey);
    }

    /**
     * @MethodName getCommonAnnouncement
     * @Params * @param null
     * @Description 获取主页公告列表
     * @Return CommonResult
     * @Since 2021/12/29
     */
    @Override
    public IPage<AnnouncementVo> getCommonAnnouncement(Integer limit, Integer currentPage) {
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;
        return announcementEntityService.getAnnouncementList(limit, currentPage, true);
    }

}