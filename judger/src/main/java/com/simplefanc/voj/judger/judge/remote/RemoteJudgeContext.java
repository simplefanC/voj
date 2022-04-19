package com.simplefanc.voj.judger.judge.remote;


import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.judger.dao.ProblemEntityService;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccountRepository;
import com.simplefanc.voj.judger.judge.remote.querier.RemoteJudgeQuerier;
import com.simplefanc.voj.judger.judge.remote.submitter.RemoteJudgeSubmitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2022/1/29 13:17
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
public class RemoteJudgeContext {

    @Resource
    private RemoteJudgeSubmitter remoteJudgeSubmitter;

    @Resource
    private RemoteJudgeQuerier remoteJudgeQuerier;

    @Autowired
    private RemoteAccountRepository remoteAccountRepository;

    @Autowired
    private ProblemEntityService problemService;

    @Async
    public void judge(ToJudge toJudge) {
        String[] source = toJudge.getRemoteJudgeProblem().split("-");
//        String remoteOj = source[0];
        RemoteOj remoteOj = RemoteOj.getTypeByName(source[0]);
        String remoteProblemId = source[1];
        RemoteAccount account = remoteAccountRepository.getRemoteAccount(remoteOj, toJudge.getUsername(), toJudge.getPassword());
        if (remoteOj == RemoteOj.JSK) {
            Problem problem = problemService.getById(toJudge.getJudge().getPid());
            remoteProblemId = problem.getInfo();
        }
        final SubmissionInfo submissionInfo = SubmissionInfo.builder()
                .remoteJudge(remoteOj)
                .remotePid(remoteProblemId)
                .remoteAccountId(toJudge.getUsername())
                .submitId(toJudge.getJudge().getSubmitId())
                .uid(toJudge.getJudge().getUid())
                .cid(toJudge.getJudge().getCid())
                .pid(toJudge.getJudge().getPid())
                .language(toJudge.getJudge().getLanguage())
                .userCode(toJudge.getJudge().getCode())
                .serverIp(toJudge.getJudgeServerIp())
                .serverPort(toJudge.getJudgeServerPort())
                .build();

        // 调用远程判题
        boolean isSubmitOk = remoteJudgeSubmitter.process(submissionInfo, account);
        if (isSubmitOk) {
            remoteJudgeQuerier.process(submissionInfo, account);
        }
    }
}