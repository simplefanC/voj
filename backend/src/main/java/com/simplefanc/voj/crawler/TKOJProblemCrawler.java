package com.simplefanc.voj.crawler;

import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.utils.JsoupUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author chenfan
 * @date 2022/1/17 23:07
 **/
@Component
public class TKOJProblemCrawler extends ProblemCrawler {
    public static final String JUDGE_NAME = "TKOJ";
    public static final String HOST = "http://tk.hustoj.com";
    public static final String PROBLEM_URL = "/problem.php?id=%s";

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        // 验证题号是否符合规范 4位数字
        Assert.isTrue(problemId.matches("\\d{4}"), "TKOJ题号格式错误！");
        Problem info = new Problem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        // 获取连接
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtils.getDocument(connection, null);
        String html = document.html();
        html = html.replaceAll("<br>", "\n");
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(ReUtil.getGroup1("<h2>" + problemId + ": ([\\s\\S]*?)</h2>", html).trim());
        info.setDescription(ReUtil.getGroup1("<h2>Description</h2><div class=\"content\">([\\s\\S]*?)</div><h2>Input</h2", html));
        info.setInput(ReUtil.getGroup1("<h2>Input</h2><div class=\"content\">([\\s\\S]*?)</div>", html));
        info.setOutput(ReUtil.getGroup1("<h2>Output</h2><div class=\"content\">([\\s\\S]*?)</div>", html));
        info.setTimeLimit(Integer.parseInt(ReUtil.getGroup1("Time Limit: </span>([1-9]\\d*) Sec", html)) * 1000);
        info.setMemoryLimit(Integer.parseInt(ReUtil.getGroup1("Memory Limit: </span>([1-9]\\d*) MB", html)));
        StringBuilder sb = new StringBuilder("<input>")
                .append(ReUtil.getGroup1("<h2>Sample Input</h2>\n" +
                        "<pre class=\"content\"><span class=\"sampledata\">([\\s\\S]*?)</span></pre>", html))
                .append("</input><output>")
                .append(ReUtil.getGroup1("<h2>Sample Output</h2>\n" +
                        "<pre class=\"content\"><span class=\"sampledata\">([\\s\\S]*?)</span></pre>", html))
                .append("</output>");
        info.setExamples(sb.toString());
        final String hint = ReUtil.getGroup1("<h2>HINT</h2><div class=\"content\">([\\s\\S]*?)</div>", html);
        info.setHint(hint != null ? hint.trim() : hint);
        info.setIsRemote(true);
        info.setSource(String.format("<a style='color:#1A5CC8' href='http://tk.hustoj.com/problem.php?id=%s'>%s</a>", problemId, JUDGE_NAME + "-" + problemId));
        info.setType(0)
                .setAuth(1)
                .setAuthor(author)
                .setOpenCaseResult(false)
                .setIsRemoveEndBlank(false)
                .setDifficulty(1);
        return new RemoteProblemInfo().setProblem(info).setTagList(null);
    }

    @Override
    public String getOjInfo() {
        return JUDGE_NAME;
    }
}
