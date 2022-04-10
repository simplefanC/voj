package com.simplefanc.voj.service.oj.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.simplefanc.voj.common.exception.StatusFailException;
import com.simplefanc.voj.dao.user.UserInfoEntityService;
import com.simplefanc.voj.dao.user.UserRecordEntityService;
import com.simplefanc.voj.pojo.entity.user.UserInfo;
import com.simplefanc.voj.pojo.vo.ACMRankVo;
import com.simplefanc.voj.pojo.vo.OIRankVo;
import com.simplefanc.voj.service.oj.RankService;
import com.simplefanc.voj.utils.Constants;
import com.simplefanc.voj.utils.RedisUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 20:47
 * @Description:
 */
@Service
public class RankServiceImpl implements RankService {

    // 排行榜缓存时间 60s
    private static final long cacheRankSecond = 60;
    @Autowired
    private UserRecordEntityService userRecordEntityService;
    @Autowired
    private UserInfoEntityService userInfoEntityService;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * @MethodName get-rank-list
     * @Params * @param null
     * @Description 获取排行榜数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    public IPage getRankList(Integer limit, Integer currentPage, String searchUser, Integer type) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        List<String> uidList = null;
        if (!StringUtils.isEmpty(searchUser)) {
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.and(wrapper -> wrapper
                    .like("username", searchUser)
                    .or()
                    .like("nickname", searchUser)
                    .or()
                    .like("realname", searchUser));

            userInfoQueryWrapper.eq("status", 0);

            uidList = userInfoEntityService.list(userInfoQueryWrapper)
                    .stream()
                    .map(UserInfo::getUuid)
                    .collect(Collectors.toList());
        }

        IPage rankList = null;
        // 根据type查询不同类型的排行榜
        if (type.intValue() == Constants.Contest.TYPE_ACM.getCode()) {
            rankList = getACMRankList(limit, currentPage, uidList);
        } else if (type.intValue() == Constants.Contest.TYPE_OI.getCode()) {
            rankList = getOIRankList(limit, currentPage, uidList);
        } else {
            throw new StatusFailException("比赛类型代码不正确！");
        }
        return rankList;
    }


    private IPage<ACMRankVo> getACMRankList(int limit, int currentPage, List<String> uidList) {

        IPage<ACMRankVo> data = null;
        if (uidList != null) {
            Page<ACMRankVo> page = new Page<>(currentPage, limit);
            if (uidList.size() > 0) {
                data = userRecordEntityService.getACMRankList(page, uidList);
            } else {
                data = page;
            }
        } else {
            String key = Constants.Account.ACM_RANK_CACHE.getCode() + "_" + limit + "_" + currentPage;
            data = (IPage<ACMRankVo>) redisUtils.get(key);
            if (data == null) {
                Page<ACMRankVo> page = new Page<>(currentPage, limit);
                data = userRecordEntityService.getACMRankList(page, null);
                redisUtils.set(key, data, cacheRankSecond);
            }
        }

        return data;
    }


    private IPage<OIRankVo> getOIRankList(int limit, int currentPage, List<String> uidList) {

        IPage<OIRankVo> data = null;
        if (uidList != null) {
            Page<OIRankVo> page = new Page<>(currentPage, limit);
            if (uidList.size() > 0) {
                data = userRecordEntityService.getOIRankList(page, uidList);
            } else {
                data = page;
            }
        } else {
            String key = Constants.Account.OI_RANK_CACHE.getCode() + "_" + limit + "_" + currentPage;
            data = (IPage<OIRankVo>) redisUtils.get(key);
            if (data == null) {
                Page<OIRankVo> page = new Page<>(currentPage, limit);
                data = userRecordEntityService.getOIRankList(page, null);
                redisUtils.set(key, data, cacheRankSecond);
            }
        }

        return data;
    }
}