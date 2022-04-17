package com.simplefanc.voj.remoteJudge.submitter;

import com.simplefanc.voj.remoteJudge.RemoteOjAware;
import com.simplefanc.voj.remoteJudge.SubmissionInfo;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;

public interface Submitter extends RemoteOjAware {
    void submit(SubmissionInfo info, RemoteAccount account) throws Exception;
}
