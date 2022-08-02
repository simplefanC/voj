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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author chenfan
 */
@Component
@Slf4j(topic = "voj")
@RefreshScope
@RequiredArgsConstructor
public class RemoteJudgeSubmitter {

    private final JudgeEntityService judgeEntityService;

    private final JudgeService judgeService;

    public boolean process(SubmissionInfo info, RemoteAccount account) {
        log.info(
                "Ready Send Task to RemoteJudge[{}] => submit_id: [{}], uid: [{}],"
                        + " pid: [{}], vjudge_username: [{}], vjudge_password: [{}]",
                info.remoteOj, info.submitId, info.uid, info.pid, account.accountId, account.password);

        String errLog = null;
        try {
            // account的context存储了相关cookie等
            LoginersHolder.getLoginer(info.remoteOj).login(account);
            SubmittersHolder.getSubmitter(info.remoteOj).submit(info, account);
        } catch (Exception e) {
            log.error("Submit Failed! Error:", e);
            errLog = e.getMessage();
        }

        // 提交失败 前端手动按按钮再次提交 修改状态 STATUS_SUBMITTED_FAILED
        if (StrUtil.isBlank(info.remoteRunId)) {
            log.error("[{}] Submit Failed! The submit_id of remote judge is blank.", info.remoteOj);

            // 更新此次提交状态为提交失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus())
                    .set("error_message", errLog).eq("submit_id", info.submitId);
            judgeEntityService.update(judgeUpdateWrapper);
            // 更新其它表
//            judgeService.updateOtherTable(
//                    new Judge().setSubmitId(info.submitId).setStatus(JudgeStatus.STATUS_SYSTEM_ERROR.getStatus())
//                            .setCid(info.cid).setUid(info.uid).setPid(info.pid));
            return false;
        }

        // 提交成功顺便更新状态为-->STATUS_JUDGING 判题中...
        judgeEntityService.updateById(new Judge().setSubmitId(info.submitId)
                .setStatus(JudgeStatus.STATUS_JUDGING.getStatus()).setVjudgeSubmitId(info.remoteRunId)
                .setVjudgeUsername(account.accountId).setVjudgePassword(account.password));

        log.info("[{}] Submit Successfully! The submit_id of remote judge is [{}]. Waiting the result of the task!",
                info.remoteRunId, info.remoteOj);
        return true;
    }

}
