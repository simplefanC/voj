package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.extra.spring.SpringUtil;
import com.simplefanc.voj.common.utils.Tools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class CrawlersHolder {

    private static HashMap<String, AbstractProblemCrawler> crawlers = new HashMap<>();

    public static AbstractProblemCrawler getCrawler(String remoteOj) {
        if (!crawlers.containsKey(remoteOj)) {
            synchronized (crawlers) {
                if (!crawlers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends AbstractProblemCrawler>> crawlerClasses = Tools.findSubClasses(
                                "com.simplefanc.voj.backend.judge.remote.crawler", AbstractProblemCrawler.class);
                        for (Class<? extends AbstractProblemCrawler> crawlerClass : crawlerClasses) {
                            AbstractProblemCrawler crawler = SpringUtil.getBean(crawlerClass);
                            crawlers.put(crawler.getOjInfo(), crawler);
                        }
                    } catch (Throwable t) {
                        log.error("Get Crawler Failed", t);
                    }
                }
            }
        }
        return crawlers.get(remoteOj);
    }

}