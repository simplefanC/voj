package com.simplefanc.voj.remoteJudge.querier;

import com.simplefanc.voj.remoteJudge.RemoteOjAware;
import com.simplefanc.voj.remoteJudge.SubmissionInfo;
import com.simplefanc.voj.remoteJudge.SubmissionRemoteStatus;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;

public interface Querier extends RemoteOjAware {
    SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) throws Exception;
}
