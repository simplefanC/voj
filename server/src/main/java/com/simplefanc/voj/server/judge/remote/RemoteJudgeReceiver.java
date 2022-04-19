package com.simplefanc.voj.server.judge.remote;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.judge.RemoteJudgeAccount;
import com.simplefanc.voj.server.common.constants.QueueConstant;
import com.simplefanc.voj.server.common.utils.RedisUtil;
import com.simplefanc.voj.server.dao.judge.JudgeEntityService;
import com.simplefanc.voj.server.dao.judge.RemoteJudgeAccountEntityService;
import com.simplefanc.voj.server.judge.AbstractReceiver;
import com.simplefanc.voj.server.judge.ChooseUtils;
import com.simplefanc.voj.server.judge.Dispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RemoteJudgeReceiver extends AbstractReceiver {

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(10);
    @Autowired
    private JudgeEntityService judgeEntityService;
    @Autowired
    private Dispatcher dispatcher;
    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private ChooseUtils chooseUtils;
    @Autowired
    private RemoteJudgeAccountEntityService remoteJudgeAccountEntityService;

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 优先处理比赛的提交
        // 其次处理普通提交的提交
        handleWaitingTask(QueueConstant.CONTEST_REMOTE_JUDGE_WAITING_HANDLE,
                QueueConstant.GENERAL_REMOTE_JUDGE_WAITING_HANDLE);
    }

    @Override
    public String getTaskByRedis(String queue) {
        if (redisUtil.lGetListSize(queue) > 0) {
            return (String) redisUtil.lrPop(queue);
        } else {
            return null;
        }
    }

    @Override
    public void handleJudgeMsg(String taskJsonStr) {
        JSONObject task = JSONUtil.parseObj(taskJsonStr);

        Judge judge = task.get("judge", Judge.class);
        String token = task.getStr("token");
        String remoteJudgeProblem = task.getStr("remoteJudgeProblem");
        String remoteOJName = remoteJudgeProblem.split("-")[0].toUpperCase();

        dispatchRemoteJudge(judge,
                token,
                remoteJudgeProblem,
                remoteOJName);
    }

    private void dispatchRemoteJudge(Judge judge, String token, String remoteJudgeProblem, String remoteOJName) {

        ToJudge toJudge = new ToJudge();
        toJudge.setJudge(judge)
                .setToken(token)
                .setRemoteJudgeProblem(remoteJudgeProblem);

        commonJudge(remoteOJName, toJudge, judge);
        // 如果队列中还有任务，则继续处理
        processWaitingTask();
    }


    private void commonJudge(String OJName, ToJudge toJudge, Judge judge) {
        // 尝试600s
        AtomicInteger tryNum = new AtomicInteger(0);
        String key = UUID.randomUUID().toString() + toJudge.getJudge().getSubmitId();
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {
                if (tryNum.get() > 200) {
                    // 获取调用多次失败可能为系统忙碌，判为提交失败
                    judge.setStatus(JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus());
                    judge.setErrorMessage("Submission failed! Please resubmit this submission again!" +
                            "Cause: Waiting for account scheduling timeout");
                    judgeEntityService.updateById(judge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        boolean isCanceled = future.cancel(true);
                        if (isCanceled) {
                            futureTaskMap.remove(key);
                        }
                    }
                    return;
                }
                tryNum.getAndIncrement();
                RemoteJudgeAccount account = chooseUtils.chooseRemoteAccount(OJName);
                if (account != null) {
                    toJudge.setUsername(account.getUsername())
                            .setPassword(account.getPassword());
                    // 调用判题服务
                    dispatcher.dispatcher("judge", "/remote-judge", toJudge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        future.cancel(true);
                        futureTaskMap.remove(key);
                    }
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 3, TimeUnit.SECONDS);
        futureTaskMap.put(key, scheduledFuture);
    }

}
