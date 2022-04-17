package com.simplefanc.voj.judge.self;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.judge.AbstractReceiver;
import com.simplefanc.voj.judge.Dispatcher;
import com.simplefanc.voj.pojo.dto.ToJudge;
import com.simplefanc.voj.pojo.entity.judge.Judge;
import com.simplefanc.voj.utils.Constants;
import com.simplefanc.voj.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @Author: chenfan
 * @Date: 2021/2/5 16:43
 * @Description:
 */
@Component
@Slf4j(topic = "voj")
public class JudgeReceiver extends AbstractReceiver {

    @Autowired
    private Dispatcher dispatcher;

    @Autowired
    private RedisUtils redisUtils;

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 优先处理比赛的提交
        // 其次处理普通提交的提交
        handleWaitingTask(Constants.Queue.CONTEST_JUDGE_WAITING.getName(),
                Constants.Queue.GENERAL_JUDGE_WAITING.getName());
    }


    @Override
    public String getTaskByRedis(String queue) {
        long size = redisUtils.lGetListSize(queue);
        if (size > 0) {
            return (String) redisUtils.lrPop(queue);
        } else {
            return null;
        }
    }

    @Override
    public void handleJudgeMsg(String taskJsonStr) {
        JSONObject task = JSONUtil.parseObj(taskJsonStr);
        Judge judge = task.get("judge", Judge.class);
        String token = task.getStr("token");
        // 调用判题服务
        dispatcher.dispatcher("judge", "/judge", new ToJudge()
                .setJudge(judge)
                .setToken(token)
                .setRemoteJudgeProblem(null));
        // 接着处理任务
        processWaitingTask();
    }

}