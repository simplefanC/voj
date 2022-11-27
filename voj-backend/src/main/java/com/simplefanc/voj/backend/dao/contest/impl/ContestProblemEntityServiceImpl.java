package com.simplefanc.voj.backend.dao.contest.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.mapper.ContestProblemMapper;
import com.simplefanc.voj.backend.pojo.vo.ContestProblemVO;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import lombok.RequiredArgsConstructor;
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
 * @since 2021-10-23
 */
@Service
@RequiredArgsConstructor
public class ContestProblemEntityServiceImpl extends ServiceImpl<ContestProblemMapper, ContestProblem>
        implements ContestProblemEntityService {

    private final ContestProblemMapper contestProblemMapper;

    private final UserInfoEntityService userInfoEntityService;

    private final ContestRecordEntityService contestRecordEntityService;

    @Override
    public List<ContestProblemVO> getContestProblemList(Long cid, Date startTime, Date endTime, Date sealTime,
                                                        Boolean isAdmin, String contestAuthorUid) {
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        superAdminUidList.add(contestAuthorUid);

        return contestProblemMapper.getContestProblemList(cid, startTime, endTime, sealTime, isAdmin,
                superAdminUidList);
    }

    @Async
    @Override
    public void syncContestRecord(Long pid, Long cid, String displayId) {

        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid).eq("cid", cid).set("display_id", displayId);
        contestRecordEntityService.update(updateWrapper);
    }

}
