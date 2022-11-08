package com.simplefanc.voj.judger.judge.remote.provider.codeforcesgym;

import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces.CFStyleLoginer;
import org.springframework.stereotype.Component;

@Component
public class GYMLoginer extends CFStyleLoginer {

    public GYMLoginer(DedicatedHttpClientFactory dedicatedHttpClientFactory) {
        super(dedicatedHttpClientFactory);
    }

    @Override
    public RemoteOjInfo getOjInfo() {
        return GYMInfo.INFO;
    }
}
