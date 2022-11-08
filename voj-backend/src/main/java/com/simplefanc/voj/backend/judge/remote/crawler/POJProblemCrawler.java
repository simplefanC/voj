package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @Author: chenfan
 * @Date: 2021/6/24 23:27
 * @Description:
 */
@Component
public class POJProblemCrawler extends ProblemCrawler {

    public static final String JUDGE_NAME = "POJ";

    public static final String HOST = "http://poj.org";

    public static final String PROBLEM_URL = "/problem?id=%s";

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId) throws Exception {
        // 验证题号是否符合规范 1-9开头的数字
        Assert.isTrue(problemId.matches("[1-9]\\d*"), "POJ题号格式错误！");
        RemoteProblemInfo problemInfo = new RemoteProblemInfo();
        Problem info = problemInfo.getProblem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        String html = HttpUtil.get(url);

        html = html.replaceAll("<br>", "\n");
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(ReUtil.get("<title>\\d{3,} -- ([\\s\\S]*?)</title>", html, 1).trim());
        info.setTimeLimit(Integer.parseInt(ReUtil.get("<b>Time Limit:</b> (\\d{3,})MS</td>", html, 1)));
        info.setMemoryLimit(Integer.parseInt(ReUtil.get("<b>Memory Limit:</b> (\\d{2,})K</td>", html, 1)) / 1024);
        info.setDescription(ReUtil
                .get("<p class=\"pst\">Description</p><div class=.*?>([\\s\\S]*?)</div><p class=\"pst\">", html, 1)
                .replaceAll("src=\"[../]*", "src=\"" + HOST + "/"));

        info.setInput(
                ReUtil.get("<p class=\"pst\">Input</p><div class=.*?>([\\s\\S]*?)</div><p class=\"pst\">", html, 1));
        info.setOutput(
                ReUtil.get("<p class=\"pst\">Output</p><div class=.*?>([\\s\\S]*?)</div><p class=\"pst\">", html, 1));

        String sb = "<input>" +
                ReUtil.get(
                        "<p class=\"pst\">Sample Input</p><pre class=.*?>([\\s\\S]*?)</pre><p class=\"pst\">", html, 1) +
                "</input><output>" +
                ReUtil.get(
                        "<p class=\"pst\">Sample Output</p><pre class=.*?>([\\s\\S]*?)</pre><p class=\"pst\">", html,
                        1) +
                "</output>";
        info.setExamples(sb);

        info.setHint(ReUtil.get("<p class=.*?>Hint</p><div class=.*?>([\\s\\S]*?)</div><p class=\"pst\">", html, 1));
        info.setSource(String.format("<a style='color:#1A5CC8' href='http://poj.org/problem?id=%s'>%s</a>", problemId,
                JUDGE_NAME + "-" + problemId));
        return problemInfo;
    }

    @Override
    public String getOjInfo() {
        return JUDGE_NAME;
    }

}