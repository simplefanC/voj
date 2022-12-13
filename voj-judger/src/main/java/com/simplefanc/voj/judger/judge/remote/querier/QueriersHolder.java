package com.simplefanc.voj.judger.judge.remote.querier;

import cn.hutool.extra.spring.SpringUtil;
import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.utils.Tools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j(topic = "voj")
public class QueriersHolder {

    private static final HashMap<RemoteOj, Querier> QUERIERS = new HashMap<>();

    public static Querier getQuerier(RemoteOj remoteOj) {
        if (!QUERIERS.containsKey(remoteOj)) {
            synchronized (QUERIERS) {
                if (!QUERIERS.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Querier>> querierClasses = Tools
                                .findSubClasses("com.simplefanc.voj.judger.judge.remote", Querier.class);
                        for (Class<? extends Querier> querierClass : querierClasses) {
                            Querier querier = SpringUtil.getBean(querierClass);
                            QUERIERS.put(querier.getOjInfo().remoteOj, querier);
                        }
                    } catch (Throwable t) {
                        log.error("Get Querier Failed", t);
                    }
                }
            }
        }
        return QUERIERS.get(remoteOj);
    }

}
