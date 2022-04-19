package com.simplefanc.voj.judger.judge.remote.submitter;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import com.simplefanc.voj.judger.judge.remote.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.loginer.LoginersHolder;
import com.simplefanc.voj.judger.service.JudgeService;
import com.simplefanc.voj.judger.service.RemoteJudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author chenfan
 */
@Component
@Slf4j(topic = "voj")
@RefreshScope
public class RemoteJudgeSubmitter {

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private RemoteJudgeService remoteJudgeService;

    @Autowired
    private JudgeService judgeService;


    @Value("${voj-judge-server.name}")
    private String judgeServerName;

    public boolean process(SubmissionInfo info, RemoteAccount account) {
        log.info("Ready Send Task to RemoteJudge[{}] => submit_id: [{}], uid: [{}]," +
                        " pid: [{}], vjudge_username: [{}], vjudge_password: [{}]",
                info.remoteJudge, info.submitId, info.uid, info.pid, account.accountId, account.password);

        String errLog = null;
        try {
            // account的context存储了相关cookie等
            LoginersHolder.getLoginer(info.remoteJudge).login(account);
            SubmittersHolder.getSubmitter(info.remoteJudge).submit(info, account);
        } catch (Exception e) {
            log.error("Submit Failed! Error:", e);
            errLog = e.getMessage();
        }

        // 提交失败 前端手动按按钮再次提交 修改状态 STATUS_SUBMITTED_FAILED
        if (StrUtil.isBlank(info.remoteRunId)) {
            // 将使用的账号放回对应列表
            log.error("[{}] Submit Failed! Begin to return the account to other task!", info.remoteJudge);
            remoteJudgeService.changeAccountStatus(info.remoteJudge.getName(), account.accountId);

            // 更新此次提交状态为提交失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus())
                    .set("error_message", errLog)
                    .eq("submit_id", info.submitId);
            judgeEntityService.update(judgeUpdateWrapper);
            // 更新其它表
            judgeService.updateOtherTable(info.submitId,
                    JudgeStatus.STATUS_SYSTEM_ERROR.getStatus(),
                    info.cid,
                    info.uid,
                    info.pid,
                    null,
                    null);
            return false;
        }

        // 提交成功顺便更新状态为-->STATUS_PENDING 判题中...
        judgeEntityService.updateById(new Judge()
                .setSubmitId(info.submitId)
                .setStatus(JudgeStatus.STATUS_PENDING.getStatus())
                .setVjudgeSubmitId(info.remoteRunId)
                .setVjudgeUsername(account.accountId)
                .setVjudgePassword(account.password)
                .setJudger(judgeServerName)
        );

        log.info("[{}] Submit Successfully! The submit_id of remote judge is [{}]. Waiting the result of the task!",
                info.remoteRunId, info.remoteJudge);
        return true;
    }
}
