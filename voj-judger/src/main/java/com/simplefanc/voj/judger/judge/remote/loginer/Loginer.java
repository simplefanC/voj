package com.simplefanc.voj.judger.judge.remote.loginer;

import com.simplefanc.voj.judger.judge.remote.RemoteOjAware;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;

public interface Loginer extends RemoteOjAware {

    void login(RemoteAccount account) throws Exception;

}
