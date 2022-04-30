package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.backend.common.utils.JsoupUtil;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author chenfan
 * @date 2022/1/17 23:06
 **/
@Component
public class MXTProblemCrawler extends ProblemCrawler {
    public static final String JUDGE_NAME = "MXT";
    public static final String HOST = "https://www.maxuetang.cn";
    public static final String PROBLEM_URL = "/course/%s.html";

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        // 验证题号是否符合规范 4位数字
        Assert.isTrue(problemId.matches("\\d{4,5}"), "MXT题号格式错误！");
        Problem info = new Problem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        // 获取连接
        Connection connection = JsoupUtil.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtil.getDocument(connection, null);
        String html = document.html();
        html = html.replaceAll("<br>", "\n");
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(ReUtil.get("<div class=\"page-header\">\n" +
                "\t\t<h2>([A-Z]\\d{4} )(\\[[\\s\\S]*?\\] )*([\\s\\S]*?)<\\/h2>\n" +
                "\t<\\/div>", html, 3));
        info.setDescription(ReUtil.getGroup1("<div class=\"panel-heading\">\n" +
                "\t\t\t<b>描述</b>\n" +
                "\t\t</div>\n" +
                "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html).trim());
        info.setInput(ReUtil.getGroup1("<div class=\"panel-heading\">\n" +
                "\t\t\t<b>输入</b>\n" +
                "\t\t</div>\n" +
                "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html).trim());
        info.setOutput(ReUtil.getGroup1("<div class=\"panel-heading\">\n" +
                "\t\t\t<b>输出</b>\n" +
                "\t\t<\\/div>\n" +
                "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html).trim());
//        info.setTimeLimit(jsonObject.getInt("time_limit"));
//        info.setMemoryLimit(jsonObject.getInt("mem_limit") / 1024);
        StringBuilder sb = new StringBuilder("<input>")
                .append(ReUtil.getGroup1("<div class=\"panel-heading\">\n" +
                        "\t\t\t<b>样例输入 </b>\n" +
                        "\t\t</div>\n" +
                        "\t\t<div class=\"panel-body\">\n" +
                        "\t\t<figure class=\"highlight\"><pre class=\"pre\" style=\"background-color:#fff\">([\\s\\S]*?)</pre></figure>\n" +
                        "\t\t</div>", html))
                .append("</input><output>")
                .append(ReUtil.getGroup1("<div class=\"panel-heading\">\n" +
                        "\t\t\t<b>样例输出 </b>\n" +
                        "\t\t</div>\n" +
                        "\t\t<div class=\"panel-body\">\n" +
                        "\t\t<figure><pre class=\"pre\" style=\"background-color:#fff\">([\\s\\S]*?)</pre></figure>\n" +
                        "\t\t</div>", html))
                .append("</output>");
        info.setExamples(sb.toString());
        final String hint = ReUtil.getGroup1("<div class=\"panel-heading\">\n" +
                "\t\t\t<b>提示</b>\n" +
                "\t\t</div>\n" +
                "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html);
        info.setHint(hint != null ? hint.trim() : hint);
        info.setIsRemote(true);
        info.setSource(String.format("<a style='color:#1A5CC8' href='https://www.maxuetang.cn/course/%s.html'>%s</a>", problemId, JUDGE_NAME + "-" + problemId));
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
