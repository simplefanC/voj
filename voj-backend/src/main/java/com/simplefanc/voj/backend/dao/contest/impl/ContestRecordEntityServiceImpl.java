package com.simplefanc.voj.backend.dao.contest.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.mapper.ContestRecordMapper;
import com.simplefanc.voj.backend.pojo.vo.ContestRecordVo;
import com.simplefanc.voj.common.constants.ContestConstant;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Service
@RequiredArgsConstructor
public class ContestRecordEntityServiceImpl extends ServiceImpl<ContestRecordMapper, ContestRecord>
        implements ContestRecordEntityService {

    private final ContestRecordMapper contestRecordMapper;

    private final UserInfoEntityService userInfoEntityService;

    private final RedisUtil redisUtil;

    @Override
    public IPage<ContestRecord> getACInfo(Integer currentPage, Integer limit, Integer status, Long cid,
                                          String contestCreatorId) {

        List<ContestRecord> acInfo = contestRecordMapper.getACInfo(status, cid);

        HashMap<Long, String> pidMapUidAndPid = new HashMap<>(12);
        HashMap<String, Long> UidAndPidMapTime = new HashMap<>(12);

        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();

        List<ContestRecord> userACInfo = new LinkedList<>();

        for (ContestRecord contestRecord : acInfo) {
            // 超级管理员和比赛创建者的提交跳过
            if (contestRecord.getUid().equals(contestCreatorId) || superAdminUidList.contains(contestRecord.getUid())) {
                continue;
            }

            contestRecord.setFirstBlood(false);
            String uidAndPid = pidMapUidAndPid.get(contestRecord.getPid());
            if (uidAndPid == null) {
                pidMapUidAndPid.put(contestRecord.getPid(), contestRecord.getUid() + contestRecord.getPid());
                UidAndPidMapTime.put(contestRecord.getUid() + contestRecord.getPid(), contestRecord.getTime());
            } else {
                Long firstTime = UidAndPidMapTime.get(uidAndPid);
                Long tmpTime = contestRecord.getTime();
                if (tmpTime < firstTime) {
                    pidMapUidAndPid.put(contestRecord.getPid(), contestRecord.getUid() + contestRecord.getPid());
                    UidAndPidMapTime.put(contestRecord.getUid() + contestRecord.getPid(), tmpTime);
                }
            }
            userACInfo.add(contestRecord);
        }

        List<ContestRecord> pageList = new ArrayList<>();

        int count = userACInfo.size();

        // 计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            ContestRecord contestRecord = userACInfo.get(currId + i);
            if (pidMapUidAndPid.get(contestRecord.getPid()).equals(contestRecord.getUid() + contestRecord.getPid())) {
                contestRecord.setFirstBlood(true);
            }
            pageList.add(contestRecord);
        }

        Page<ContestRecord> page = new Page<>(currentPage, limit);
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);

        return page;
    }

    @Override
    public List<ContestRecordVo> getOIContestRecord(Contest contest, Boolean isOpenSealRank) {
        String oiRankScoreType = contest.getOiRankScoreType();
        Long cid = contest.getId();
        Date sealTime = contest.getSealRankTime();
        Date startTime = contest.getStartTime();
        Date endTime = contest.getEndTime();

        if (!isOpenSealRank) {
            // 封榜解除 获取最新数据
            // 获取每个用户每道题最近一次提交
            if (Objects.equals(ContestConstant.OI_RANK_RECENT_SCORE, oiRankScoreType)) {
                return contestRecordMapper.getOIContestRecordByRecentSubmission(cid, false, sealTime,
                        startTime, endTime);
            } else {
                return contestRecordMapper.getOIContestRecordByHighestSubmission(cid, false, sealTime,
                        startTime, endTime);
            }

        } else {
            String key = ContestConstant.OI_CONTEST_RANK_CACHE + "_" + oiRankScoreType + "_" + cid;
            List<ContestRecordVo> oiContestRecordList = (List<ContestRecordVo>) redisUtil.get(key);
            if (oiContestRecordList == null) {
                if (Objects.equals(ContestConstant.OI_RANK_RECENT_SCORE, oiRankScoreType)) {
                    oiContestRecordList = contestRecordMapper.getOIContestRecordByRecentSubmission(cid,
                            true, sealTime, startTime, endTime);
                } else {
                    oiContestRecordList = contestRecordMapper.getOIContestRecordByHighestSubmission(cid,
                            true, sealTime, startTime, endTime);
                }
                redisUtil.set(key, oiContestRecordList, 2 * 3600);
            }
            return oiContestRecordList;
        }

    }

    @Override
    public List<ContestRecordVo> getACMContestRecord(String username, Long cid) {
        return contestRecordMapper.getACMContestRecord(username, cid);
    }

}
