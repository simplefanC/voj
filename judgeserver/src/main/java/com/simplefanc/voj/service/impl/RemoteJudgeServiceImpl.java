package com.simplefanc.voj.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.dao.RemoteJudgeAccountEntityService;
import com.simplefanc.voj.pojo.entity.judge.RemoteJudgeAccount;
import com.simplefanc.voj.service.RemoteJudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/12/7 23:57
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
public class RemoteJudgeServiceImpl implements RemoteJudgeService {

    @Autowired
    private RemoteJudgeAccountEntityService remoteJudgeAccountEntityService;

    @Override
    public void changeAccountStatus(String remoteJudge, String username) {

        UpdateWrapper<RemoteJudgeAccount> remoteJudgeAccountUpdateWrapper = new UpdateWrapper<>();
        remoteJudgeAccountUpdateWrapper.set("status", true)
                .eq("username", username);
        remoteJudgeAccountUpdateWrapper.eq("oj", remoteJudge);

        boolean isOk = remoteJudgeAccountEntityService.update(remoteJudgeAccountUpdateWrapper);

        // 重试8次
        if (!isOk) {
            tryAgainUpdateAccount(remoteJudgeAccountUpdateWrapper, remoteJudge, username);
        }
    }

    private void tryAgainUpdateAccount(UpdateWrapper<RemoteJudgeAccount> updateWrapper, String remoteJudge, String username) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean success = remoteJudgeAccountEntityService.update(updateWrapper);
            if (success) {
                return;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("Remote Judge：Change Account status to `true` Failed ----------->{}", "oj:" + remoteJudge + ",username:" + username);
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (retryable);
    }
}