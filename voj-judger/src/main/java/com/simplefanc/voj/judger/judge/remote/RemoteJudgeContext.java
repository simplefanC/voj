package com.simplefanc.voj.judger.judge.remote;


import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
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

//    @Async // 去掉 异步注解 否则直接返回主服务器 无法实现对判题机的负载均衡
    public void judge(ToJudge toJudge) {
        String[] source = toJudge.getRemoteJudgeProblem().split("-");
//        String remoteOj = source[0];
        RemoteOj remoteOj = RemoteOj.getTypeByName(source[0]);
        String remoteProblemId = source[1];
        RemoteAccount account = remoteAccountRepository.getRemoteAccount(remoteOj, toJudge.getUsername(), toJudge.getPassword());
        final Judge judge = toJudge.getJudge();
        if (remoteOj == RemoteOj.JSK) {
            Problem problem = problemService.getById(judge.getPid());
            remoteProblemId = problem.getInfo();
        }
        final SubmissionInfo submissionInfo = SubmissionInfo.builder()
                .remoteJudge(remoteOj)
                .remotePid(remoteProblemId)
                .remoteAccountId(toJudge.getUsername())
                .submitId(judge.getSubmitId())
                .uid(judge.getUid())
                .cid(judge.getCid())
                .pid(judge.getPid())
                .language(judge.getLanguage())
                .userCode(judge.getCode())
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