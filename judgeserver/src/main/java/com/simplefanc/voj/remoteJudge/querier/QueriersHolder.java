package com.simplefanc.voj.remoteJudge.querier;

import cn.hutool.extra.spring.SpringUtil;
import com.simplefanc.voj.pojo.RemoteOj;
import com.simplefanc.voj.util.Tools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class QueriersHolder {

    private static HashMap<RemoteOj, Querier> queriers = new HashMap<>();

    public static Querier getQuerier(RemoteOj remoteOj) {
        if (!queriers.containsKey(remoteOj)) {
            synchronized (queriers) {
                if (!queriers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends Querier>> querierClasses = Tools.findSubClasses("com.simplefanc.voj.remoteJudge", Querier.class);
                        for (Class<? extends Querier> querierClass : querierClasses) {
                            Querier querier = SpringUtil.getBean(querierClass);
                            queriers.put(querier.getOjInfo().remoteOj, querier);
                        }
                    } catch (Throwable t) {
                        log.error("Get Querier Failed", t);
                    }
                }
            }
        }
        return queriers.get(remoteOj);
    }

}
