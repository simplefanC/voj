package com.simplefanc.voj.remoteJudge.loginer;

import com.simplefanc.voj.remoteJudge.RemoteOjAware;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;

public interface Loginer extends RemoteOjAware {
    void login(RemoteAccount account) throws Exception;
}
