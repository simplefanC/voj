package com.simplefanc.voj.remoteJudge.task.Impl;


/**
 * @Author: chenfan
 * @Date: 2021/11/6 11:17
 * @Description:
 */
public class GYMJudge extends CodeForcesJudge {
    @Override
    protected String getSubmitUrl(String contestNum) {
        return IMAGE_HOST + "/gym/" + contestNum + "/submit";
    }
}