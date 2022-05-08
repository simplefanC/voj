package com.simplefanc.voj.judger.service.impl;

import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.judger.common.exception.SystemError;
import com.simplefanc.voj.judger.dao.ContestRecordEntityService;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import com.simplefanc.voj.judger.dao.ProblemEntityService;
import com.simplefanc.voj.judger.dao.UserAcproblemEntityService;
import com.simplefanc.voj.judger.judge.local.JudgeContext;
import com.simplefanc.voj.judger.judge.remote.RemoteJudgeContext;
import com.simplefanc.voj.judger.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2022/3/12 15:54
 * @Description:
 */
@Service
@RefreshScope
public class JudgeServiceImpl implements JudgeService {

    @Value("${voj-judge-server.name}")
    private String judgeServerName;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private JudgeContext judgeContext;

    @Autowired
    private RemoteJudgeContext remoteJudgeContext;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Override
    public void judge(Judge judge) {
        // 标志该判题过程进入编译阶段
        judge.setStatus(JudgeStatus.STATUS_COMPILING.getStatus());
        // 写入当前判题服务的名字
        judge.setJudger(judgeServerName);
        judgeEntityService.updateById(judge);
        Problem problem = problemEntityService.getById(judge.getPid());
        // 【进行判题操作】！！！
        Judge finalJudge = judgeContext.judge(problem, judge);

        // 更新该次提交
        judgeEntityService.updateById(finalJudge);

        if (finalJudge.getStatus().intValue() != JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus()) {
            // 更新其它表
            updateOtherTable(finalJudge);
        }
    }

    @Override
    public void remoteJudge(ToJudge toJudge) {
        remoteJudgeContext.judge(toJudge);
    }

    @Override
    public Boolean compileSpj(String code, Long pid, String spjLanguage, HashMap<String, String> extraFiles)
            throws SystemError {
        return judgeContext.compileSpj(code, pid, spjLanguage, extraFiles);
    }

    @Override
    public Boolean compileInteractive(String code, Long pid, String interactiveLanguage,
                                      HashMap<String, String> extraFiles) throws SystemError {
        return judgeContext.compileInteractive(code, pid, interactiveLanguage, extraFiles);
    }

    @Override
    public void updateOtherTable(Judge judge) {
        // 非比赛提交
        if (judge.getCid() == 0) {
            // 如果是AC，就更新user_acproblem表
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