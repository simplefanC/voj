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

    public String literal;

    public HttpHost mainHost;

    public String defaultChaset = "UTF-8";

    /**
     * In milliseconds
     */
    public long maxInactiveInterval = 300000L;

    public String _64IntIoFormat;

    public String urlForIndexDisplay;

    //////////////////////////////////////////////////////////////////////////////

    public RemoteOjInfo(RemoteOj remoteOj, String literal, HttpHost mainHost) {
        this.remoteOj = remoteOj;
        this.literal = literal;
        this.mainHost = mainHost;
        this.urlForIndexDisplay = mainHost.toURI();
    }

    @Override
    public String toString() {
        return literal;
    }

}
