package com.simplefanc.voj.judger.dao.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.judger.dao.ContestRecordEntityService;
import com.simplefanc.voj.judger.mapper.ContestRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author chenfan
 * @since 2021-10-23
 */
@Service
@RequiredArgsConstructor
public class ContestRecordEntityServiceImpl extends ServiceImpl<ContestRecordMapper, ContestRecord>
        implements ContestRecordEntityService {

    private static final List<Integer> PENALTY_STATUS = Arrays.asList(JudgeStatus.STATUS_PRESENTATION_ERROR.getStatus(),
            JudgeStatus.STATUS_WRONG_ANSWER.getStatus(), JudgeStatus.STATUS_TIME_LIMIT_EXCEEDED.getStatus(),
            JudgeStatus.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus(), JudgeStatus.STATUS_RUNTIME_ERROR.getStatus());

    private final ContestRecordMapper contestRecordMapper;

    @Override
    public void updateContestRecord(Judge judge) {
        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        // 如果是AC
        if (judge.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
            updateWrapper.set("status", ContestEnum.RECORD_AC.getCode());
            // 部分通过
        } else if (judge.getStatus().intValue() == JudgeStatus.STATUS_PARTIAL_ACCEPTED.getStatus()) {
            updateWrapper.set("status", ContestEnum.RECORD_NOT_AC_NOT_PENALTY.getCode());
            // 需要被罚时的状态
        } else if (PENALTY_STATUS.contains(judge.getStatus())) {
            updateWrapper.set("status", ContestEnum.RECORD_NOT_AC_PENALTY.getCode());

        } else {
            updateWrapper.set("status", ContestEnum.RECORD_NOT_AC_NOT_PENALTY.getCode());
        }

        if (judge.getScore() != null) {
            updateWrapper.set("score", judge.getScore());
        }

        updateWrapper.set("use_time", judge.getTime());
        // submit_id一定只有一个
        updateWrapper.eq("submit_id", judge.getSubmitId())
                .eq("cid", judge.getCid())
                .eq("uid", judge.getUid());
        boolean result = contestRecordMapper.update(null, updateWrapper) > 0;
        if (!result) {
            tryAgainUpdate(updateWrapper);
        }
    }

    public void tryAgainUpdate(UpdateWrapper<ContestRecord> updateWrapper) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean result = contestRecordMapper.update(null, updateWrapper) > 0;
            if (result) {
                break;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("更新contest_record表超过最大重试次数");
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        }
        while (retryable);
    }

}
