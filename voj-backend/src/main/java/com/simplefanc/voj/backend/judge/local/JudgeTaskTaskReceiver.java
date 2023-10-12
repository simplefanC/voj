package com.simplefanc.voj.backend.judge.local;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.backend.common.constants.CallJudgerType;
import com.simplefanc.voj.backend.common.constants.QueueConstant;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.judge.AbstractTaskReceiver;
import com.simplefanc.voj.backend.judge.Dispatcher;
import com.simplefanc.voj.common.pojo.dto.JudgeDTO;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Author: chenfan
 * @Date: 2021/2/5 16:43
 * @Description:
 */
@Component
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class JudgeTaskTaskReceiver extends AbstractTaskReceiver {

    private final Dispatcher dispatcher;

    private final RedisUtil redisUtil;

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 优先处理比赛的提交
        // 其次处理普通提交的提交
        this.handleWaitingTask(QueueConstant.CONTEST_JUDGE_WAITING, QueueConstant.GENERAL_JUDGE_WAITING);
    }

    @Override
    public String getTaskFromRedis(String queue) {
        long size = redisUtil.lGetListSize(queue);
        if (size > 0) {
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
        // 调用判题服务
        dispatcher.dispatcher(CallJudgerType.JUDGE, "/judge",
                new JudgeDTO().setJudge(judge).setToken(token).setRemoteJudgeProblem(null));
        // 接着处理任务
        processWaitingTask();
    }

}