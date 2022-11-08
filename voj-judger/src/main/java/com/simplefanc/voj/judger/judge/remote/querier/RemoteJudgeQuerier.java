package com.simplefanc.voj.judger.judge.remote.querier;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.judger.dao.ContestRecordEntityService;
import com.simplefanc.voj.judger.dao.JudgeCaseEntityService;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import com.simplefanc.voj.judger.dao.UserAcproblemEntityService;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionRemoteStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenfan
 */
@Slf4j(topic = "voj")
@Component
@RequiredArgsConstructor
public class RemoteJudgeQuerier {

    private final static ScheduledExecutorService SCHEDULER = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final static Map<String, ScheduledFuture<?>> FUTURE_TASK_MAP = new ConcurrentHashMap<>(
            Runtime.getRuntime().availableProcessors() * 2);

    private final JudgeEntityService judgeEntityService;

    private final JudgeCaseEntityService judgeCaseEntityService;

    private final UserAcproblemEntityService userAcproblemEntityService;

    private final ContestRecordEntityService contestRecordEntityService;

    public void process(SubmissionInfo info, RemoteAccount account) {
        String key = UUID.randomUUID().toString() + info.submitId;

        ScheduledFuture<?> scheduledFuture = SCHEDULER.scheduleWithFixedDelay(new QueryTask(info, account, key), 0, 3,
                TimeUnit.SECONDS);
        FUTURE_TASK_MAP.put(key, scheduledFuture);
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
                Querier querier = QueriersHolder.getQuerier(info.remoteOj);
                final SubmissionRemoteStatus result = querier.query(info, account);
                checkSubmissionResult(result);
            } catch (Exception e) {
                log.error("The Error of getting the `remote judge` result:", e);
            }
        }

        private void checkSubmissionResult(SubmissionRemoteStatus result) {
            JudgeStatus status = result.getStatusType() != null ? result.getStatusType()
                    : JudgeStatus.STATUS_SYSTEM_ERROR;
            if (status == JudgeStatus.STATUS_COMPILING || status == JudgeStatus.STATUS_JUDGING) {
                recordMidResult(status);
            } else {
                log.info("[{}] Get Result Successfully! Status:[{}]", info.remoteOj, status);

                // 保留各个测试点的结果数据
                if (!CollectionUtils.isEmpty(result.getJudgeCaseList())) {
                    judgeCaseEntityService.saveBatch(result.getJudgeCaseList());
                }

                Judge judge = wrapResultToJudge(result, status);
                // 写回数据库
                judgeEntityService.updateById(judge);
                // 同步其它表
                // 非比赛提交
                if (judge.getCid() == 0) {
                    // 如果是AC，就更新 user_acproblem表
                    if (JudgeStatus.STATUS_ACCEPTED.getStatus().equals(judge.getStatus())) {
                        userAcproblemEntityService.saveOrUpdate(new UserAcproblem().setPid(judge.getPid())
                                .setUid(judge.getUid()).setSubmitId(judge.getSubmitId()));
                    }
                } else {
                    // 如果是比赛提交
                    contestRecordEntityService.updateContestRecord(judge);
                }
                cancelFutureTask();
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
                        "There is something wrong with the " + info.remoteOj + ", please try again later");
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

            log.error("[{}] Get Result Failed!", info.remoteOj);
            cancelFutureTask();
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
