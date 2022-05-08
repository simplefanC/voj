package com.simplefanc.voj.judger.judge.remote.querier;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import com.simplefanc.voj.judger.judge.remote.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.service.JudgeService;
import com.simplefanc.voj.judger.service.RemoteJudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenfan
 */
@Slf4j(topic = "voj")
@Component
public class RemoteJudgeQuerier {

    private final static ScheduledExecutorService SCHEDULER = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final static Map<String, Future> FUTURE_TASK_MAP = new ConcurrentHashMap<>(
            Runtime.getRuntime().availableProcessors() * 2);

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private JudgeService judgeService;

    @Autowired
    private RemoteJudgeService remoteJudgeService;

    public void process(SubmissionInfo info, RemoteAccount account) {
        String key = UUID.randomUUID().toString() + info.submitId;

        ScheduledFuture<?> beeperHandle = SCHEDULER.scheduleWithFixedDelay(new QueryTask(info, account, key), 0, 3,
                TimeUnit.SECONDS);
        FUTURE_TASK_MAP.put(key, beeperHandle);
    }

    private void releaseRemoteJudgeAccount(String remoteJudge, String username, String resultSubmitId) {
        log.info("After Get Result,remote_judge:[{}],submit_id: [{}]! Begin to return the account to other task!",
                remoteJudge, resultSubmitId);
        // 将账号变为可用
        remoteJudgeService.changeAccountStatus(remoteJudge, username);
    }

    class QueryTask implements Runnable {

        AtomicInteger count = new AtomicInteger(0);

        SubmissionInfo info;

        RemoteAccount account;

        String key;

        public QueryTask(SubmissionInfo info, RemoteAccount account, String key) {
            this.info = info;
            this.account = account;
            this.key = key;
        }

        @Override
        public void run() {
            // 超过20次失败则判为提交失败
            if (count.get() > 20) {
                handleQueryFailure();
                return;
            }

            count.getAndIncrement();
            try {
                Querier querier = QueriersHolder.getQuerier(info.remoteJudge);
                final SubmissionRemoteStatus result = querier.query(info, account);
                checkSubmissionResult(result);
            } catch (Exception e) {
                log.error("The Error of getting the `remote judge` result:", e);
            }
        }

        private void checkSubmissionResult(SubmissionRemoteStatus result) {
            JudgeStatus status = result.getStatusType() != null ? result.getStatusType()
                    : JudgeStatus.STATUS_SYSTEM_ERROR;
            if (status != JudgeStatus.STATUS_PENDING && status != JudgeStatus.STATUS_JUDGING
                    && status != JudgeStatus.STATUS_COMPILING) {
                log.info("[{}] Get Result Successfully! Status:[{}]", info.remoteJudge, status);

                releaseRemoteJudgeAccount(info.remoteJudge.getName(), account.accountId, info.remoteRunId);

                Judge judge = wrapResultToJudge(result, status);
                // 写回数据库
                judgeEntityService.updateById(judge);
                // 同步其它表
                judgeService.updateOtherTable(judge);
                cancelFutureTask();
            } else {
                recordMidResult(status);
            }
        }

        private Judge wrapResultToJudge(SubmissionRemoteStatus result, JudgeStatus status) {
            Judge judge = new Judge();
            judge.setSubmitId(info.submitId).setCid(info.cid).setUid(info.uid).setPid(info.pid)
                    .setStatus(status.getStatus()).setTime(result.getExecutionTime())
                    .setMemory(result.getExecutionMemory());

            if (status == JudgeStatus.STATUS_COMPILE_ERROR) {
                judge.setErrorMessage(result.getCompilationErrorInfo());
            } else if (status == JudgeStatus.STATUS_SYSTEM_ERROR) {
                judge.setErrorMessage(
                        "There is something wrong with the " + info.remoteJudge + ", please try again later");
            }

            // 如果是比赛题目，需要特别适配OI比赛的得分 除AC给100 其它结果给0分
            if (info.cid != 0) {
                judge.setScore(status == JudgeStatus.STATUS_ACCEPTED ? 100 : 0);
            }
            return judge;
        }

        private void recordMidResult(JudgeStatus status) {
            Judge judge = new Judge();
            judge.setSubmitId(info.submitId).setStatus(status.getStatus());
            // 写回数据库
            judgeEntityService.updateById(judge);
        }

        private void handleQueryFailure() {
            // 更新此次提交状态为提交失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus()).set("error_message",
                    "Waiting for remote judge result exceeds the maximum number of times, please try submitting again!")
                    .eq("submit_id", info.submitId);
            judgeEntityService.update(judgeUpdateWrapper);

            log.error("[{}] Get Result Failed!", info.remoteJudge);
            releaseRemoteJudgeAccount(info.remoteJudge.getName(), account.accountId, info.remoteRunId);
            cancelFutureTask();
        }

        private void cancelFutureTask() {
            Future future = FUTURE_TASK_MAP.get(key);
            if (future != null) {
                boolean isCanceled = future.cancel(true);
                if (isCanceled) {
                    FUTURE_TASK_MAP.remove(key);
                }
            }
        }

    }

}
