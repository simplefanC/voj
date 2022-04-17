package com.simplefanc.voj.remoteJudge.provider.mxt;

import com.simplefanc.voj.pojo.RemoteOj;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import org.apache.http.HttpHost;

public class MXTInfo {
    public static final RemoteOjInfo INFO = new RemoteOjInfo(
            RemoteOj.MXT,
            "MXT",
            new HttpHost("www.maxuetang.cn", 443, "https")
    );
}
