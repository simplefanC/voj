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

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors() * 2);
    @Autowired
    private JudgeEntityService judgeEntityService;
    @Autowired
    private JudgeService judgeService;
    @Autowired
    private RemoteJudgeService remoteJudgeService;

    // TODO 行数过多
    public void process(SubmissionInfo info, RemoteAccount account) {
        Querier querier = QueriersHolder.getQuerier(info.remoteJudge);
        String key = UUID.randomUUID().toString() + info.submitId;
        AtomicInteger count = new AtomicInteger(0);
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {
                // 超过20次失败则判为提交失败
                if (count.get() > 20) {
                    // 更新此次提交状态为提交失败！
                    UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                    judgeUpdateWrapper.set("status", JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus())
                            .set("error_message", "Waiting for remote judge result exceeds the maximum number of times, please try submitting again!")
                            .eq("submit_id", info.submitId);
                    judgeEntityService.update(judgeUpdateWrapper);

                    log.error("[{}] Get Result Failed!", info.remoteJudge);
                    changeRemoteJudgeLock(info.remoteJudge.getName(), account.accountId, info.remoteRunId);

                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        boolean isCanceled = future.cancel(true);
                        if (isCanceled) {
                            futureTaskMap.remove(key);
                        }
                    }
                    return;
                }

                count.getAndIncrement();
                try {
                    final SubmissionRemoteStatus result = querier.query(info, account);
                    JudgeStatus status = result.getStatusType() != null ? result.getStatusType() : JudgeStatus.STATUS_SYSTEM_ERROR;
                    if (status != JudgeStatus.STATUS_PENDING &&
                            status != JudgeStatus.STATUS_JUDGING &&
                            status != JudgeStatus.STATUS_COMPILING) {
                        log.info("[{}] Get Result Successfully! Status:[{}]", info.remoteJudge, status);

                        changeRemoteJudgeLock(info.remoteJudge.getName(), account.accountId, info.remoteRunId);

                        Judge judge = new Judge();
                        judge.setSubmitId(info.submitId)
                                .setStatus(status.getStatus())
                                .setTime(result.getExecutionTime())
                                .setMemory(result.getExecutionMemory());

                        if (status == JudgeStatus.STATUS_COMPILE_ERROR) {
                            judge.setErrorMessage(result.getCompilationErrorInfo());
                        } else if (status == JudgeStatus.STATUS_SYSTEM_ERROR) {
                            judge.setErrorMessage("There is something wrong with the " + info.remoteJudge + ", please try again later");
                        }

                        // 如果是比赛题目，需要特别适配OI比赛的得分 除AC给100 其它结果给0分
                        if (info.cid != 0) {
                            int score = 0;

                            if (status == JudgeStatus.STATUS_ACCEPTED) {
                                score = 100;
                            }

                            judge.setScore(score);
                            // 写回数据库
                            judgeEntityService.updateById(judge);
                            // 同步其它表
                            judgeService.updateOtherTable(info.submitId, status.getStatus(), info.cid, info.uid, info.pid, score, judge.getTime());
                        } else {
                            judgeEntityService.updateById(judge);
                            // 同步其它表
                            judgeService.updateOtherTable(info.submitId, status.getStatus(), info.cid, info.uid, info.pid, null, null);
                        }

                        Future future = futureTaskMap.get(key);
                        if (future != null) {
                            future.cancel(true);
                            futureTaskMap.remove(key);
                        }
                    } else {
                        Judge judge = new Judge();
                        judge.setSubmitId(info.submitId)
                                .setStatus(status.getStatus());
                        // 写回数据库
                        judgeEntityService.updateById(judge);
                    }
                } catch (Exception e) {
                    log.error("The Error of getting the `remote judge` result:", e);
                }
            }
        };
        ScheduledFuture<?> beeperHandle = scheduler.scheduleWithFixedDelay(
                getResultTask, 0, 3, TimeUnit.SECONDS);
        futureTaskMap.put(key, beeperHandle);
    }


    private void changeRemoteJudgeLock(String remoteJudge, String username, String resultSubmitId) {
        log.info("After Get Result,remote_judge:[{}],submit_id: [{}]! Begin to return the account to other task!",
                remoteJudge, resultSubmitId);
        // 将账号变为可用
        remoteJudgeService.changeAccountStatus(remoteJudge, username);
    }


}
