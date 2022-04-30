package com.simplefanc.voj.backend.common.constants;

/**
 * 等待判题的redis队列
 *
 * @Since 2021/12/22
 */
public interface QueueConstant {
    String CONTEST_JUDGE_WAITING = "Contest_Waiting_Handle_Queue";
    String GENERAL_JUDGE_WAITING = "General_Waiting_Handle_Queue";
    String CONTEST_REMOTE_JUDGE_WAITING_HANDLE = "Contest_Remote_Waiting_Handle_Queue";
    String GENERAL_REMOTE_JUDGE_WAITING_HANDLE = "General_Remote_Waiting_Handle_Queue";
}
