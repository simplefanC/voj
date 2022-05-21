package com.simplefanc.voj.backend.service.admin.training;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.constants.TrainingEnum;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingProblemEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingRecordEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingRegisterEntityService;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.common.pojo.entity.training.TrainingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 20:09
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class AdminTrainingRecordService {

    private final TrainingEntityService trainingEntityService;

    private final TrainingProblemEntityService trainingProblemEntityService;

    private final TrainingRecordEntityService trainingRecordEntityService;

    private final TrainingRegisterEntityService trainingRegisterEntityService;

    private final JudgeEntityService judgeEntityService;

    @Async
    public void syncUserSubmissionToRecordByTid(Long tid, String uid) {
        QueryWrapper<TrainingProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid);
        List<TrainingProblem> trainingProblemList = trainingProblemEntityService.list(queryWrapper);
        List<Long> pidList = new ArrayList<>();
        HashMap<Long, Long> pidMapTPid = new HashMap<>();
        for (TrainingProblem trainingProblem : trainingProblemList) {
            pidList.add(trainingProblem.getPid());
            pidMapTPid.put(trainingProblem.getPid(), trainingProblem.getId());
        }
        if (!CollectionUtils.isEmpty(pidList)) {
            QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
            judgeQueryWrapper.in("pid", pidList).eq("cid", 0)
                    // 只同步ac的提交
                    .eq("status", JudgeStatus.STATUS_ACCEPTED.getStatus()).eq("uid", uid);
            List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);
            saveBatchNewRecordByJudgeList(judgeList, tid, null, pidMapTPid);
        }
    }

    @Async
    public void syncAlreadyRegisterUserRecord(Long tid, Long pid, Long tpId) {
        Training training = trainingEntityService.getById(tid);
        if (!TrainingEnum.AUTH_PRIVATE.getValue().equals(training.getAuth())) {
            return;
        }
        List<String> uidList = trainingRegisterEntityService.getAlreadyRegisterUidList(tid);
        syncNewProblemUserSubmissionToRecord(pid, tpId, tid, uidList);
    }

    @Async
    public void checkSyncRecord(Training training) {
        if (!TrainingEnum.AUTH_PRIVATE.getValue().equals(training.getAuth())) {
            return;
        }
        QueryWrapper<TrainingRecord> trainingRecordQueryWrapper = new QueryWrapper<>();
        trainingRecordQueryWrapper.eq("tid", training.getId());
        int count = trainingRecordEntityService.count(trainingRecordQueryWrapper);
        if (count <= 0) {
            syncAllUserProblemRecord(training.getId());
        }

    }

    private void syncNewProblemUserSubmissionToRecord(Long pid, Long tpId, Long tid, List<String> uidList) {
        if (!CollectionUtils.isEmpty(uidList)) {
            QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
            judgeQueryWrapper.eq("pid", pid).eq("cid", 0)
                    // 只同步ac的提交
                    .eq("status", JudgeStatus.STATUS_ACCEPTED.getStatus()).in("uid", uidList);
            List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);
            saveBatchNewRecordByJudgeList(judgeList, tid, tpId, null);
        }
    }

    private void syncAllUserProblemRecord(Long tid) {
        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid);
        List<TrainingProblem> trainingProblemList = trainingProblemEntityService.list(trainingProblemQueryWrapper);
        if (trainingProblemList.size() == 0) {
            return;
        }
        List<Long> pidList = new ArrayList<>();
        HashMap<Long, Long> pidMapTPid = new HashMap<>();
        for (TrainingProblem trainingProblem : trainingProblemList) {
            pidList.add(trainingProblem.getPid());
            pidMapTPid.put(trainingProblem.getPid(), trainingProblem.getId());
        }

        List<String> uidList = trainingRegisterEntityService.getAlreadyRegisterUidList(tid);
        if (uidList.size() == 0) {
            return;
        }
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.in("pid", pidList).eq("cid", 0)
                // 只同步ac的提交
                .eq("status", JudgeStatus.STATUS_ACCEPTED.getStatus()).in("uid", uidList);
        List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);
        saveBatchNewRecordByJudgeList(judgeList, tid, null, pidMapTPid);
    }

    private void saveBatchNewRecordByJudgeList(List<Judge> judgeList, Long tid, Long tpId,
                                               HashMap<Long, Long> pidMapTPid) {
        if (!CollectionUtils.isEmpty(judgeList)) {
            List<TrainingRecord> trainingRecordList = new ArrayList<>();
            for (Judge judge : judgeList) {
                TrainingRecord trainingRecord = new TrainingRecord().setPid(judge.getPid())
                        .setSubmitId(judge.getSubmitId()).setTid(tid).setUid(judge.getUid());
                if (pidMapTPid != null) {
                    trainingRecord.setTpid(pidMapTPid.get(judge.getPid()));
                }
                if (tpId != null) {
                    trainingRecord.setTpid(tpId);
                }
                trainingRecordList.add(trainingRecord);
            }
            trainingRecordEntityService.saveBatch(trainingRecordList);
        }
    }

}