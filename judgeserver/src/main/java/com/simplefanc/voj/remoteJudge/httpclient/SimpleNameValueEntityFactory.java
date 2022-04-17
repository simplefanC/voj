package com.simplefanc.voj.remoteJudge.httpclient;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SimpleNameValueEntityFactory {

    /**
     * @param keyValues if size is odd, the last one is charset
     * @return
     */
    public static UrlEncodedFormEntity create(String... keyValues) {
        List<NameValuePair> nvps = new ArrayList<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            nvps.add(new BasicNameValuePair(keyValues[i], keyValues[i + 1]));
        }
        String charset = keyValues.length % 2 == 1 ? keyValues[keyValues.length - 1] : "UTF-8";
        return new UrlEncodedFormEntity(nvps, Charset.forName(charset));
    }

}
