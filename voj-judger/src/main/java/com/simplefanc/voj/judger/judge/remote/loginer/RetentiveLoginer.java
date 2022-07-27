package com.simplefanc.voj.judger.judge.remote.loginer;

import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 保持 登录
 * <p>
 * 未实现 RemoteOjAware.getOjInfo()
 */
public abstract class RetentiveLoginer implements Loginer {

    /**
     * 记录上次登录时间戳 httpContext hashCode -> last login epoch millisecond
     */
    private final static ConcurrentHashMap<Integer, Long> lastLoginTimeMap = new ConcurrentHashMap<>();

    @Override
    public final void login(RemoteAccount account) throws Exception {
        int contextHashCode = account.getContext().hashCode();
        Long lastLoginTime = lastLoginTimeMap.get(contextHashCode);
        // 没有context 或者 超过5分钟 重新登录
        if (lastLoginTime == null || now() - lastLoginTime > getOjInfo().maxInactiveInterval) {
            loginEnforce(account);
            lastLoginTimeMap.put(contextHashCode, now());
        }
    }

    private long now() {
        return System.currentTimeMillis();
    }

    protected abstract void loginEnforce(RemoteAccount account) throws Exception;

}
