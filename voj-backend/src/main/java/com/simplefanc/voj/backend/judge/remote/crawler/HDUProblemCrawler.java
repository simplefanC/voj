package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @Author: chenfan
 * @Date: 2021/2/17 22:42
 * @Description:
 */
@Component
public class HDUProblemCrawler extends ProblemCrawler {

    public static final String JUDGE_NAME = "HDU";

    public static final String HOST = "https://acm.hdu.edu.cn";

    public static final String PROBLEM_URL = "/showproblem.php?pid=%s";

    /**
     * @param problemId String的原因是因为某些题库题号不是纯数字
     * @return 返回Problem对象
     * @throws Exception
     */
    @Override
    public RemoteProblemInfo getProblemInfo(String problemId) throws Exception {
        // 验证题号是否符合规范
        Assert.isTrue(problemId.matches("[1-9]\\d*"), "HDU题号格式错误！");
        RemoteProblemInfo problemInfo = new RemoteProblemInfo();
        Problem info = problemInfo.getProblem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        String html = HttpUtil.get(url);
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(ReUtil.get("color:#1A5CC8'>([\\s\\S]*?)</h1>", html, 1).trim());
        info.setTimeLimit(Integer.parseInt(ReUtil.get("(\\d*) MS", html, 1)));
        info.setMemoryLimit(Integer.parseInt(ReUtil.get("/(\\d*) K", html, 1)) / 1024);
        info.setDescription(ReUtil.get(">Problem Description</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1)
                .replaceAll("src=[./]*", "src=" + HOST + "/"));
        info.setInput(ReUtil.get(">Input</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1));
        info.setOutput(ReUtil.get(">Output</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1));
        StringBuilder sb = new StringBuilder("<input>");
        sb.append(ReUtil.get(">Sample Input</div><div .*?,monospace;\">([\\s\\S]*?)</div></pre>", html, 1));
        sb.append("</input><output>");
        sb.append(ReUtil.get(
                ">Sample Output</div><div .*?monospace;\">([\\s\\S]*?)(<div style=.*?</div><i style=.*?</i>)*?</div></pre>",
                html, 1)).append("</output>");
        info.setExamples(sb.toString());
        info.setHint(ReUtil.get("<i>Hint</i></div>([\\s\\S]*?)</div><i .*?<br><[^<>]*?panel_title[^<>]*?>", html, 1));
        info.setSource(
                String.format("<a style='color:#1A5CC8' href='https://acm.hdu.edu.cn/showproblem.php?pid=%s'>%s</a>",
                        problemId, JUDGE_NAME + "-" + problemId));

        return problemInfo;
    }

    @Override
    public String getOjInfo() {
        return JUDGE_NAME;
    }

}