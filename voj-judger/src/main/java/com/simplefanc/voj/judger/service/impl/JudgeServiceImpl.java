package com.simplefanc.voj.judger.service.impl;

import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.dto.JudgeDTO;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.judger.common.exception.SystemException;
import com.simplefanc.voj.judger.dao.ContestRecordEntityService;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import com.simplefanc.voj.judger.dao.ProblemEntityService;
import com.simplefanc.voj.judger.dao.UserAcproblemEntityService;
import com.simplefanc.voj.judger.judge.local.JudgeContext;
import com.simplefanc.voj.judger.judge.remote.RemoteJudgeContext;
import com.simplefanc.voj.judger.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2022/3/12 15:54
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final JudgeEntityService judgeEntityService;

    private final ProblemEntityService problemEntityService;

    private final UserAcproblemEntityService userAcproblemEntityService;

    private final ContestRecordEntityService contestRecordEntityService;

    private final JudgeContext judgeContext;

    private final RemoteJudgeContext remoteJudgeContext;

    @Override
    public void localJudge(Judge judge) {
        Problem problem = problemEntityService.getById(judge.getPid());
        // 【进行判题操作】！！！
        judgeContext.judge(judge, problem);

        // 更新该次提交
        judgeEntityService.updateById(judge);

        if (judge.getStatus().intValue() != JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus()) {
            // 更新其它表
            // 非比赛提交
            if (judge.getCid() == 0) {
                // 如果是AC，就更新 user_acproblem表
                if (JudgeStatus.STATUS_ACCEPTED.getStatus().equals(judge.getStatus())) {
                    userAcproblemEntityService.saveOrUpdate(new UserAcproblem().setPid(judge.getPid())
                            .setUid(judge.getUid()).setSubmitId(judge.getSubmitId()));
                }
            } else {
                // 如果是比赛提交
                contestRecordEntityService.updateContestRecord(judge);
            }
        }
    }

    @Override
    public void remoteJudge(JudgeDTO toJudge) {
        remoteJudgeContext.judge(toJudge);
    }

    @Override
    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles)
            throws SystemException {
        return judgeContext.compileSpj(code, pid, spjLanguage, extraFiles);
    }

    @Override
    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage,
                                      HashMap<String, String> extraFiles) throws SystemException {
        return judgeContext.compileInteractive(code, pid, interactiveLanguage, extraFiles);
    }

}