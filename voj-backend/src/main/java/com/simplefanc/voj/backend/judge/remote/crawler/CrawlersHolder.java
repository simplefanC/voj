package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.extra.spring.SpringUtil;
import com.simplefanc.voj.common.utils.Tools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class CrawlersHolder {

    private static HashMap<String, ProblemCrawler> crawlers = new HashMap<>();

    public static ProblemCrawler getCrawler(String remoteOj) {
        if (!crawlers.containsKey(remoteOj)) {
            synchronized (crawlers) {
                if (!crawlers.containsKey(remoteOj)) {
                    try {
                        List<Class<? extends ProblemCrawler>> crawlerClasses = Tools.findSubClasses(
                                "com.simplefanc.voj.backend.judge.remote.crawler", ProblemCrawler.class);
                        for (Class<? extends ProblemCrawler> crawlerClass : crawlerClasses) {
                            ProblemCrawler crawler = SpringUtil.getBean(crawlerClass);
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