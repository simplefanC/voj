package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.constants.AccountConstant;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.service.admin.user.UserRecordService;
import com.simplefanc.voj.backend.pojo.vo.ACMRankVo;
import com.simplefanc.voj.backend.pojo.vo.OIRankVo;
import com.simplefanc.voj.backend.service.oj.RankService;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 20:47
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {

    // 排行榜缓存时间 60s
    private static final long cacheRankSecond = 60;

    private final UserRecordService userRecordService;

    private final UserInfoEntityService userInfoEntityService;

    private final RedisUtil redisUtil;

    /**
     * @MethodName get-rank-list
     * @Params * @param null
     * @Description 获取排行榜数据
     * @Return CommonResult
     * @Since 2021/10/27
     */
    @Override
    public IPage getRankList(Integer limit, Integer currentPage, String searchUser, Integer type) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        List<String> uidList = null;
        if (!StrUtil.isEmpty(searchUser)) {
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.and(wrapper -> wrapper.like("username", searchUser).or().like("nickname", searchUser)
                    .or().like("realname", searchUser));

            userInfoQueryWrapper.eq("status", 0);

            uidList = userInfoEntityService.list(userInfoQueryWrapper).stream().map(UserInfo::getUuid)
                    .collect(Collectors.toList());
        }

        IPage rankList = null;
        // 根据type查询不同类型的排行榜
        if (type.intValue() == ContestEnum.TYPE_ACM.getCode()) {
            rankList = getACMRankList(limit, currentPage, uidList);
        } else if (type.intValue() == ContestEnum.TYPE_OI.getCode()) {
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
                data = userRecordService.getACMRankList(page, uidList);
            } else {
                data = page;
            }
        } else {
            String key = AccountConstant.ACM_RANK_CACHE + "_" + limit + "_" + currentPage;
            data = (IPage<ACMRankVo>) redisUtil.get(key);
            if (data == null) {
                Page<ACMRankVo> page = new Page<>(currentPage, limit);
                data = userRecordService.getACMRankList(page, null);
                redisUtil.set(key, data, cacheRankSecond);
            }
        }

        return data;
    }

    private IPage<OIRankVo> getOIRankList(int limit, int currentPage, List<String> uidList) {

        IPage<OIRankVo> data = null;
        if (uidList != null) {
            Page<OIRankVo> page = new Page<>(currentPage, limit);
            if (uidList.size() > 0) {
                data = userRecordService.getOIRankList(page, uidList);
            } else {
                data = page;
            }
        } else {
            String key = AccountConstant.OI_RANK_CACHE + "_" + limit + "_" + currentPage;
            data = (IPage<OIRankVo>) redisUtil.get(key);
            if (data == null) {
                Page<OIRankVo> page = new Page<>(currentPage, limit);
                data = userRecordService.getOIRankList(page, null);
                redisUtil.set(key, data, cacheRankSecond);
            }
        }

        return data;
    }

}