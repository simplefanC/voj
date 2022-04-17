package com.simplefanc.voj.remoteJudge.httpclient;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;

public class CookieUtil {

    public static String getCookieValue(DedicatedHttpClient client, String name) {
        String value = null;
        CookieStore cookieStore = (CookieStore) client.getContext().getAttribute(HttpClientContext.COOKIE_STORE);
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals(name)) {
                value = cookie.getValue();
            }
        }
        return value;
    }
}
