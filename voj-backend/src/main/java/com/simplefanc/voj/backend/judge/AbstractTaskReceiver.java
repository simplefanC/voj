package com.simplefanc.voj.backend.judge;

/**
 * @Author: chenfan
 * @Date: 2021/12/22 12:40
 * @Description:
 */
public abstract class AbstractTaskReceiver {

    public void handleWaitingTask(String... queues) {
        for (String queue : queues) {
            String taskJsonStr = getTaskFromRedis(queue);
            if (taskJsonStr != null) {
                handleTask(taskJsonStr);
            }
        }
    }

    public abstract String getTaskFromRedis(String queue);

    public abstract void handleTask(String taskJsonStr);

}