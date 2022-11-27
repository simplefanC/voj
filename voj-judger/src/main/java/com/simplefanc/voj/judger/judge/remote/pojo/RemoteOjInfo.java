package com.simplefanc.voj.judger.judge.remote.pojo;

import com.simplefanc.voj.common.constants.RemoteOj;
import org.apache.http.HttpHost;

/**
 * Once initiated, don't modify it. I don't bother implementing an immutable one.
 *
 * @author chenfan
 */
public class RemoteOjInfo {

    public RemoteOj remoteOj;


    public HttpHost mainHost;

    public String defaultCharset = "UTF-8";

    /**
     * In milliseconds
     */
    public long maxInactiveInterval = 300000L;

    public RemoteOjInfo(RemoteOj remoteOj, HttpHost mainHost) {
        this.remoteOj = remoteOj;
        this.mainHost = mainHost;
    }

}
