package com.simplefanc.voj.server.judge.local;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.server.common.constants.QueueConstant;
import com.simplefanc.voj.server.common.utils.RedisUtil;
import com.simplefanc.voj.server.dao.judge.JudgeEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @Author: chenfan
 * @Date: 2021/2/5 16:44
 * @Description:
 */
@Component
@Slf4j(topic = "voj")
@RefreshScope
public class JudgeDispatcher {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private com.simplefanc.voj.server.judge.local.JudgeReceiver judgeReceiver;

    @Value("${voj.judge.token}")
    private String judgeToken;

    public void sendTask(Judge judge, Boolean isContest) {
        JSONObject task = new JSONObject();
        task.set("judge", judge);
        task.set("token", judgeToken);
        task.set("isContest", isContest);
        try {
            boolean isOk;
            if (isContest) {
                isOk = redisUtil.llPush(QueueConstant.CONTEST_JUDGE_WAITING, JSONUtil.toJsonStr(task));
            } else {
                isOk = redisUtil.llPush(QueueConstant.GENERAL_JUDGE_WAITING, JSONUtil.toJsonStr(task));
            }
            if (!isOk) {
                judgeEntityService.updateById(new Judge()
                        .setSubmitId(judge.getSubmitId())
                        .setStatus(JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus())
                        .setErrorMessage("Please try to submit again!")
                );
            }
            // 调用判题任务处理
            judgeReceiver.processWaitingTask();
        } catch (Exception e) {
            log.error("调用redis将判题纳入判题等待队列异常,此次判题任务判为系统错误--------------->{}", e.getMessage());
            judgeEntityService.failToUseRedisPublishJudge(judge.getSubmitId(), judge.getPid(), isContest);
        }
    }
}