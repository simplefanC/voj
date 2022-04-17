package com.simplefanc.voj.remoteJudge.loginer;

import cn.hutool.extra.spring.SpringUtil;
import com.simplefanc.voj.pojo.RemoteOj;
import com.simplefanc.voj.util.Tools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class LoginersHolder {

    private static HashMap<RemoteOj, Loginer> loginers = new HashMap<>();

    public static Loginer getLoginer(RemoteOj remoteOj) {
        if (!loginers.containsKey(remoteOj)) {
            synchronized (loginers) {
                if (!loginers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Loginer>> loginerClasses = Tools.findSubClasses("com.simplefanc.voj.remoteJudge", Loginer.class);
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
