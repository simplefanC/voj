package com.simplefanc.voj.judger.judge.remote.querier;

import com.simplefanc.voj.judger.judge.remote.RemoteOjAware;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionRemoteStatus;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;

public interface Querier extends RemoteOjAware {

    SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) throws Exception;

}
