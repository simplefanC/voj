package com.simplefanc.voj.server.dao.contest.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.server.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.server.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.server.dao.user.UserInfoEntityService;
import com.simplefanc.voj.server.mapper.ContestProblemMapper;
import com.simplefanc.voj.server.pojo.vo.ContestProblemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Service
public class ContestProblemEntityServiceImpl extends ServiceImpl<ContestProblemMapper, ContestProblem> implements ContestProblemEntityService {

    @Autowired
    private ContestProblemMapper contestProblemMapper;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Override
    public List<ContestProblemVo> getContestProblemList(Long cid, Date startTime, Date endTime, Date sealTime, Boolean isAdmin, String contestAuthorUid) {
        // 筛去 比赛管理员和超级管理员的提交
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        superAdminUidList.add(contestAuthorUid);

        return contestProblemMapper.getContestProblemList(cid, startTime, endTime, sealTime, isAdmin, superAdminUidList);
    }

    @Async
    @Override
    public void syncContestRecord(Long pid, Long cid, String displayId) {

        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid)
                .eq("cid", cid)
                .set("display_id", displayId);
        contestRecordEntityService.update(updateWrapper);
    }
}
