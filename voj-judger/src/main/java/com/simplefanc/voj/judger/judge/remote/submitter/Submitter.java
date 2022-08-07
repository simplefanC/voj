package com.simplefanc.voj.judger.judge.remote.submitter;

import com.simplefanc.voj.judger.judge.remote.RemoteOjAware;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;

public interface Submitter extends RemoteOjAware {

    void submit(SubmissionInfo info, RemoteAccount account) throws Exception;

}
