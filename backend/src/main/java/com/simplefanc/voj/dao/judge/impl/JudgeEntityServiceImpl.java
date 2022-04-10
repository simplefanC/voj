package com.simplefanc.voj.dao.judge.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.dao.judge.JudgeEntityService;
import com.simplefanc.voj.mapper.JudgeMapper;
import com.simplefanc.voj.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.pojo.entity.judge.Judge;
import com.simplefanc.voj.pojo.vo.JudgeVo;
import com.simplefanc.voj.pojo.vo.ProblemCountVo;
import com.simplefanc.voj.utils.Constants;

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
@Slf4j(topic = "voj")
public class JudgeEntityServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeEntityService {

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;


    @Override
    public IPage<JudgeVo> getCommonJudgeList(Integer limit,
                                             Integer currentPage,
                                             String searchPid,
                                             Integer status,
                                             String username,
                                             String uid,
                                             Boolean completeProblemID) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        return judgeMapper.getCommonJudgeList(page, searchPid, status, username, uid, completeProblemID);
    }

    @Override
    public IPage<JudgeVo> getContestJudgeList(Integer limit, Integer currentPage, String displayId, Long cid, Integer status,
                                              String username, String uid, Boolean beforeContestSubmit, String rule,
                                              Date startTime, Date sealRankTime, String sealTimeUid, Boolean completeProblemID) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        return judgeMapper.getContestJudgeList(page, displayId, cid, status, username, uid, beforeContestSubmit,
                rule, startTime, sealRankTime, sealTimeUid, completeProblemID);
    }


    @Override
    public void failToUseRedisPublishJudge(Long submitId, Long pid, Boolean isContest) {
        UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
        judgeUpdateWrapper.eq("submit_id", submitId)
                .set("error_message", "The something has gone wrong with the data Backup server. Please report this to administrator.")
                .set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
        judgeMapper.update(null, judgeUpdateWrapper);
        // 更新contest_record表
        if (isContest) {
            UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("submit_id", submitId) // submit_id一定只有一个
                    .set("first_blood", false)
                    .set("status", Constants.Contest.RECORD_NOT_AC_NOT_PENALTY.getCode());
            contestRecordEntityService.update(updateWrapper);
        }
    }

    @Override
    public ProblemCountVo getContestProblemCount(Long pid, Long cpid, Long cid, Date startTime, Date sealRankTime, List<String> adminList) {
        return judgeMapper.getContestProblemCount(pid, cpid, cid, startTime, sealRankTime, adminList);
    }

    @Override
    public ProblemCountVo getProblemCount(Long pid) {
        return judgeMapper.getProblemCount(pid);
    }

    @Override
    public int getTodayJudgeNum() {
        return judgeMapper.getTodayJudgeNum();
    }

    @Override
    public List<ProblemCountVo> getProblemListCount(List<Long> pidList) {
        return judgeMapper.getProblemListCount(pidList);
    }

}
