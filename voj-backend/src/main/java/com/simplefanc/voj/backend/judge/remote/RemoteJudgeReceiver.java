package com.simplefanc.voj.backend.judge.remote;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.backend.common.constants.CallJudgerType;
import com.simplefanc.voj.backend.common.constants.QueueConstant;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.judge.AbstractReceiver;
import com.simplefanc.voj.backend.judge.ChooseUtils;
import com.simplefanc.voj.backend.judge.Dispatcher;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.dto.ToJudge;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.judge.RemoteJudgeAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RemoteJudgeReceiver extends AbstractReceiver {

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(10);

    private final JudgeEntityService judgeEntityService;

    private final Dispatcher dispatcher;

    private final RedisUtil redisUtil;

    private final ChooseUtils chooseUtils;

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
        String remoteOjName = remoteJudgeProblem.split("-")[0].toUpperCase();

        dispatchRemoteJudge(judge, token, remoteJudgeProblem, remoteOjName);
    }

    private void dispatchRemoteJudge(Judge judge, String token, String remoteJudgeProblem, String remoteOjName) {
        ToJudge toJudge = new ToJudge();
        toJudge.setJudge(judge).setToken(token).setRemoteJudgeProblem(remoteJudgeProblem);

        commonJudge(remoteOjName, toJudge, judge);
        // 如果队列中还有任务，则继续处理
        processWaitingTask();
    }

    private void commonJudge(String ojName, ToJudge toJudge, Judge judge) {
        String key = UUID.randomUUID().toString() + toJudge.getJudge().getSubmitId();
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(
                new RemoteJudgeAccountTask(ojName, toJudge, judge, key), 0, 3, TimeUnit.SECONDS);
        futureTaskMap.put(key, scheduledFuture);
    }

    class RemoteJudgeAccountTask implements Runnable {

        String ojName;

        ToJudge toJudge;

        Judge judge;

        String key;

        // 尝试600s
        AtomicInteger tryNum = new AtomicInteger(0);

        public RemoteJudgeAccountTask(String ojName, ToJudge toJudge, Judge judge, String key) {
            this.ojName = ojName;
            this.toJudge = toJudge;
            this.judge = judge;
            this.key = key;
        }

        @Override
        public void run() {
            if (tryNum.get() > 200) {
                // 获取调用多次失败可能为系统忙碌，判为提交失败
                judge.setStatus(JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus());
                judge.setErrorMessage("Submission failed! Please resubmit this submission again!"
                        + "Cause: Waiting for account scheduling timeout");
                judgeEntityService.updateById(judge);
                cancelFutureTask();
                return;
            }
            tryNum.getAndIncrement();
            RemoteJudgeAccount account = chooseUtils.chooseRemoteAccount(ojName);
            if (account != null) {
                toJudge.setUsername(account.getUsername()).setPassword(account.getPassword());
                // 调用判题服务
                dispatcher.dispatcher(CallJudgerType.JUDGE, "/remote-judge", toJudge);
                cancelFutureTask();
            }
        }

        private void cancelFutureTask() {
            Future future = futureTaskMap.get(key);
            if (future != null) {
                boolean isCanceled = future.cancel(true);
                if (isCanceled) {
                    futureTaskMap.remove(key);
                }
            }
        }

    }

}
