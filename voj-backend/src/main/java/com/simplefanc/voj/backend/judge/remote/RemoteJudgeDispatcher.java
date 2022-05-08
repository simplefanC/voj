package com.simplefanc.voj.backend.judge.remote;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.backend.common.constants.QueueConstant;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@Slf4j(topic = "voj")
@RefreshScope
public class RemoteJudgeDispatcher {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private RemoteJudgeReceiver remoteJudgeReceiver;

    @Value("${voj.judge.token}")
    private String judgeToken;

    public void sendTask(Judge judge, String remoteJudgeProblem, Boolean isContest) {
        JSONObject task = new JSONObject();
        task.set("judge", judge);
        task.set("remoteJudgeProblem", remoteJudgeProblem);
        task.set("token", judgeToken);
        task.set("isContest", isContest);
        try {
            boolean isOk;
            if (isContest) {
                isOk = redisUtil.llPush(QueueConstant.CONTEST_REMOTE_JUDGE_WAITING_HANDLE, JSONUtil.toJsonStr(task));
            } else {
                isOk = redisUtil.llPush(QueueConstant.GENERAL_REMOTE_JUDGE_WAITING_HANDLE, JSONUtil.toJsonStr(task));
            }
            if (!isOk) {
                judgeEntityService.updateById(new Judge().setSubmitId(judge.getSubmitId())
                        .setStatus(JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus())
                        .setErrorMessage("Please try to submit again!"));
            }
            remoteJudgeReceiver.processWaitingTask();
        } catch (Exception e) {
            log.error("调用redis将判题纳入判题等待队列异常,此次判题任务判为系统错误--------------->", e);
            judgeEntityService.failToUseRedisPublishJudge(judge.getSubmitId(), judge.getPid(), isContest);
        }
    }

}
