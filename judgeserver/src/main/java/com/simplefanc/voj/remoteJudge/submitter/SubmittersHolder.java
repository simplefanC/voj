package com.simplefanc.voj.remoteJudge.submitter;

import cn.hutool.extra.spring.SpringUtil;
import com.simplefanc.voj.pojo.RemoteOj;
import com.simplefanc.voj.util.Tools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class SubmittersHolder {
    private static HashMap<RemoteOj, Submitter> submitters = new HashMap<>();

    public static Submitter getSubmitter(RemoteOj remoteOj) {
        if (!submitters.containsKey(remoteOj)) {
            synchronized (submitters) {
                if (!submitters.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Submitter>> submitterClasses = Tools.findSubClasses("com.simplefanc.voj.remoteJudge", Submitter.class);
                        for (Class<? extends Submitter> submitterClass : submitterClasses) {
                            Submitter submitter = SpringUtil.getBean(submitterClass);
                            submitters.put(submitter.getOjInfo().remoteOj, submitter);
                        }
                    } catch (Throwable t) {
                        log.error("Get Submitter Failed", t);
                    }
                }
            }
        }
        return submitters.get(remoteOj);
    }
}
