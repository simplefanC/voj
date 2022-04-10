package com.simplefanc.voj.remoteJudge.task;

import com.simplefanc.voj.remoteJudge.entity.RemoteJudgeDTO;
import com.simplefanc.voj.remoteJudge.entity.RemoteJudgeRes;
import lombok.Getter;
import lombok.Setter;

;


/**
 * 远程评测抽象类
 */
public abstract class RemoteJudgeStrategy {

    @Setter
    @Getter
    private RemoteJudgeDTO remoteJudgeDTO;

    public abstract void submit();

    public abstract RemoteJudgeRes result();

    public abstract void login();

    public abstract String getLanguage(String language);

}
