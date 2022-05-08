package com.simplefanc.voj.judger.judge.remote.loginer;

import cn.hutool.extra.spring.SpringUtil;
import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.utils.Tools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j(topic = "voj")
public class LoginersHolder {

    private static HashMap<RemoteOj, Loginer> loginers = new HashMap<>();

    public static Loginer getLoginer(RemoteOj remoteOj) {
        if (!loginers.containsKey(remoteOj)) {
            synchronized (loginers) {
                if (!loginers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Loginer>> loginerClasses = Tools
                                .findSubClasses("com.simplefanc.voj.judger.judge.remote", Loginer.class);
                        for (Class<? extends Loginer> loginerClass : loginerClasses) {
                            Loginer loginer = SpringUtil.getBean(loginerClass);
                            loginers.put(loginer.getOjInfo().remoteOj, loginer);
                        }
                    } catch (Throwable t) {
                        log.error("Get Loginer Failed", t);
                    }
                }
            }
        }
        return loginers.get(remoteOj);
    }

}
