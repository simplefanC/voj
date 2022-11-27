package com.simplefanc.voj.judger.judge.remote.provider.codefores;

import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces.AbstractCFStyleSubmitter;
import org.springframework.stereotype.Component;

@Component
public class CFSubmitter extends AbstractCFStyleSubmitter {

    public static final String SUBMIT_URL = "/contest/%s/submit";

    public CFSubmitter(DedicatedHttpClientFactory dedicatedHttpClientFactory) {
        super(dedicatedHttpClientFactory);
    }

    @Override
    public RemoteOjInfo getOjInfo() {
        return CFInfo.INFO;
    }

    protected String getSubmitUrl(String contestNum) {
        return String.format(SUBMIT_URL, contestNum);
    }
}
