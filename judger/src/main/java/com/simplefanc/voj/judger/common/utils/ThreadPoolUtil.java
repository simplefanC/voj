package com.simplefanc.voj.judger.common.utils;

import java.util.concurrent.*;

/**
 * @Author: chenfan
 * @Date: 2021/12/21 12:06
 * @Description:
 */
public class ThreadPoolUtil {

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();
    private static ExecutorService executorService;

    private ThreadPoolUtil() {
        //手动创建线程池.
        executorService = new ThreadPoolExecutor(
                cpuNum, // 核心线程数
                cpuNum * 2, // 最大线程数。最多几个线程并发。
                3,//当非核心线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS,// 结束线程时间单位
                new LinkedBlockingDeque<>(200 * cpuNum), //阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());//队列满了，尝试去和最早的竞争，也不会抛出异常！
    }

    public static ThreadPoolUtil getInstance() {
        return PluginConfigHolder.INSTANCE;
    }

    public ExecutorService getThreadPool() {
        return executorService;
    }

    private static class PluginConfigHolder {
        private final static ThreadPoolUtil INSTANCE = new ThreadPoolUtil();
    }

}