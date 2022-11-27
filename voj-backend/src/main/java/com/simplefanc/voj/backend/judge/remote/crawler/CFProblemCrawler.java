package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.common.constants.RemoteOj;
import org.springframework.stereotype.Component;

/**
 * @Author: chenfan
 * @Date: 2021/7/25 9:00
 * @Description:
 */
@Component
public class CFProblemCrawler extends AbstractCFStyleProblemCrawler {
    private static final String PROBLEM_URL = "/problemset/problem/%s/%s";

    @Override
    public String getOjInfo() {
        return RemoteOj.CF.getName();
    }

    public String getProblemUrl(String contestId, String problemNum) {
        return HOST + String.format(PROBLEM_URL, contestId, problemNum);
    }

    public String getProblemSource(String html, String problemId, String contestId, String problemNum) {
        return String.format("<p>Problem：<a style='color:#1A5CC8' href='https://codeforces.com/problemset/problem/%s/%s'>%s</a></p><p>" +
                        "Contest：" + ReUtil.get("(<a[^<>]+/contest/\\d+\">.+?</a>)", html, 1)
                        .replace("/contest", HOST + "/contest")
                        .replace("color: black", "color: #009688;") + "</p>",
                contestId, problemNum, getOjInfo() + "-" + problemId);
    }
}