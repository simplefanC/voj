package com.simplefanc.voj.judger.judge.remote.provider.codeforcesgym;

import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces.CFStyleSubmitter;
import org.springframework.stereotype.Component;

@Component
public class GYMSubmitter extends CFStyleSubmitter {

    public GYMSubmitter(DedicatedHttpClientFactory dedicatedHttpClientFactory) {
        super(dedicatedHttpClientFactory);
    }

    @Override
    public RemoteOjInfo getOjInfo() {
        return GYMInfo.INFO;
    }

    protected String getSubmitUrl(String contestNum) {
        return "/gym/" + contestNum + "/submit";
    }
}
