package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author chenfan
 * @date 2022/1/17 23:06
 **/
@Component
public class MXTProblemCrawler extends AbstractProblemCrawler {

    public static final String JUDGE_NAME = "MXT";

    public static final String HOST = "https://mxt.cn";

    public static final String PROBLEM_URL = "/course/%s.html";

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId) throws Exception {
        // 验证题号是否符合规范 4位数字
        Assert.isTrue(problemId.matches("\\d{4,5}"), "MXT题号格式错误！");
        RemoteProblemInfo problemInfo = new RemoteProblemInfo();
        Problem info = problemInfo.getProblem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        String html = HttpUtil.get(url);
        html = html.replaceAll("<br>", "\n");
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(ReUtil.get("<div class=\"page-header\">\n"
                + "\t\t<h2>([A-Z]\\d{4} )(\\[[\\s\\S]*?\\] )*([\\s\\S]*?)<\\/h2>\n" + "\t<\\/div>", html, 3));
        info.setDescription(ReUtil.getGroup1("<div class=\"panel-heading\">\n" + "\t\t\t<b>描述</b>\n" + "\t\t</div>\n"
                + "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html).trim());
        info.setInput(ReUtil.getGroup1("<div class=\"panel-heading\">\n" + "\t\t\t<b>输入</b>\n" + "\t\t</div>\n"
                + "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html).trim());
        info.setOutput(ReUtil.getGroup1("<div class=\"panel-heading\">\n" + "\t\t\t<b>输出</b>\n" + "\t\t<\\/div>\n"
                + "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html).trim());
        // info.setTimeLimit(jsonObject.getInt("time_limit"));
        // info.setMemoryLimit(jsonObject.getInt("mem_limit") / 1024);
        String sb = "<input>" + ReUtil.getGroup1("<div class=\"panel-heading\">\n"
                + "\t\t\t<b>样例输入 </b>\n" + "\t\t</div>\n" + "\t\t<div class=\"panel-body\">\n"
                + "\t\t<figure class=\"highlight\"><pre class=\"pre\" style=\"background-color:#fff\">([\\s\\S]*?)</pre></figure>\n"
                + "\t\t</div>", html) +
                "</input><output>" +
                ReUtil.getGroup1("<div class=\"panel-heading\">\n" + "\t\t\t<b>样例输出 </b>\n" + "\t\t</div>\n"
                        + "\t\t<div class=\"panel-body\">\n"
                        + "\t\t<figure><pre class=\"pre\" style=\"background-color:#fff\">([\\s\\S]*?)</pre></figure>\n"
                        + "\t\t</div>", html) +
                "</output>";
        info.setExamples(sb);
        final String hint = ReUtil.getGroup1("<div class=\"panel-heading\">\n" + "\t\t\t<b>提示</b>\n" + "\t\t</div>\n"
                + "\t\t<div class=\"panel-body jdc-latex-show\">([\\s\\S]*?)</div>", html);
        info.setHint(hint != null ? hint.trim() : hint);
        info.setSource(String.format("<a style='color:#1A5CC8' href='https://mxt.cn/course/%s.html'>%s</a>",
                problemId, JUDGE_NAME + "-" + problemId));
        return problemInfo;
    }

    @Override
    public String getOjInfo() {
        return JUDGE_NAME;
    }

}
