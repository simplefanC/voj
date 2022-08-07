package com.simplefanc.voj.judger.judge.remote;

import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import com.simplefanc.voj.judger.dao.ProblemEntityService;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccountRepository;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.querier.RemoteJudgeQuerier;
import com.simplefanc.voj.judger.judge.remote.submitter.RemoteJudgeSubmitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2022/1/29 13:17
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class RemoteJudgeContext {

    private final RemoteJudgeSubmitter remoteJudgeSubmitter;

    private final RemoteJudgeQuerier remoteJudgeQuerier;

    private final RemoteAccountRepository remoteAccountRepository;

    private final ProblemEntityService problemService;

    private final JudgeEntityService judgeEntityService;

    // @Async // 去掉 异步注解 否则直接返回主服务器 无法实现对判题机的负载均衡
    public void judge(ToJudge toJudge) {
        String[] source = toJudge.getRemoteJudgeProblem().split("-");
        // String remoteOj = source[0];
        RemoteOj remoteOj = RemoteOj.getTypeByName(source[0]);
        String remoteProblemId = source[1];
        RemoteAccount account = remoteAccountRepository.getRemoteAccount(remoteOj, toJudge.getUsername(),
                toJudge.getPassword());
        final Judge judge = toJudge.getJudge();
        if (remoteOj == RemoteOj.JSK) {
            Problem problem = problemService.getById(judge.getPid());
            remoteProblemId = problem.getInfo();
        }
        final SubmissionInfo submissionInfo = SubmissionInfo.builder().remoteOj(remoteOj).remotePid(remoteProblemId)
                .remoteAccountId(toJudge.getUsername()).submitId(judge.getSubmitId()).uid(judge.getUid())
                .cid(judge.getCid()).pid(judge.getPid()).language(judge.getLanguage()).userCode(judge.getCode())
                .serverIp(toJudge.getJudgeServerIp()).serverPort(toJudge.getJudgeServerPort()).build();

        judge.setStatus(JudgeStatus.STATUS_PENDING.getStatus());
        judgeEntityService.updateById(judge);
        // 调用远程判题
        boolean isSubmitOk = remoteJudgeSubmitter.process(submissionInfo, account);
        if (isSubmitOk) {
            remoteJudgeQuerier.process(submissionInfo, account);
        }
    }

}