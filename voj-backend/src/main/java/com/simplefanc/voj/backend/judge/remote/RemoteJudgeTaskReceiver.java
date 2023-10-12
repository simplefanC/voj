package com.simplefanc.voj.backend.judge.remote;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.backend.common.constants.CallJudgerType;
import com.simplefanc.voj.backend.common.constants.QueueConstant;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.judge.AbstractTaskReceiver;
import com.simplefanc.voj.backend.judge.ChooseUtils;
import com.simplefanc.voj.backend.judge.Dispatcher;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.dto.JudgeDTO;
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
public class RemoteJudgeTaskReceiver extends AbstractTaskReceiver {

    private final static ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(10);

    private final static Map<String, ScheduledFuture<?>> FUTURE_TASK_MAP = new ConcurrentHashMap<>(10);

    private final JudgeEntityService judgeEntityService;

    private final Dispatcher dispatcher;

    private final RedisUtil redisUtil;

    private final ChooseUtils chooseUtils;

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 优先处理比赛的提交
        // 其次处理普通提交的提交
        this.handleWaitingTask(QueueConstant.CONTEST_REMOTE_JUDGE_WAITING_HANDLE,
                QueueConstant.GENERAL_REMOTE_JUDGE_WAITING_HANDLE);
    }

    @Override
    public String getTaskFromRedis(String queue) {
        if (redisUtil.lGetListSize(queue) > 0) {
            return (String) redisUtil.lrPop(queue);
        } else {
            return null;
        }
    }

    @Override
    public void handleTask(String taskJsonStr) {
        JSONObject task = JSONUtil.parseObj(taskJsonStr);

        Judge judge = task.get("judge", Judge.class);
        String token = task.getStr("token");
        String remoteJudgeProblem = task.getStr("remoteJudgeProblem");
        String remoteOjName = remoteJudgeProblem.split("-")[0].toUpperCase();

        dispatchRemoteJudge(judge, token, remoteJudgeProblem, remoteOjName);
    }

    private void dispatchRemoteJudge(Judge judge, String token, String remoteJudgeProblem, String remoteOjName) {
        JudgeDTO toJudge = new JudgeDTO();
        toJudge.setJudge(judge).setToken(token).setRemoteJudgeProblem(remoteJudgeProblem);

        commonJudge(remoteOjName, toJudge, judge);
        // 如果队列中还有任务，则继续处理
        processWaitingTask();
    }

    private void commonJudge(String ojName, JudgeDTO toJudge, Judge judge) {
        String key = UUID.randomUUID().toString() + toJudge.getJudge().getSubmitId();
        ScheduledFuture<?> scheduledFuture = SCHEDULER.scheduleWithFixedDelay(
                new RemoteJudgeAccountTask(ojName, toJudge, judge, key), 0, 3, TimeUnit.SECONDS);
        FUTURE_TASK_MAP.put(key, scheduledFuture);
    }

    class RemoteJudgeAccountTask implements Runnable {

        String ojName;

        JudgeDTO toJudge;

        Judge judge;

        String key;

        // 尝试600s
        AtomicInteger tryNum = new AtomicInteger(0);

        public RemoteJudgeAccountTask(String ojName, JudgeDTO toJudge, Judge judge, String key) {
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
            ScheduledFuture<?> future = FUTURE_TASK_MAP.get(key);
            if (future != null) {
                boolean isCanceled = future.cancel(true);
                if (isCanceled) {
                    FUTURE_TASK_MAP.remove(key);
                }
            }
        }

    }

}
