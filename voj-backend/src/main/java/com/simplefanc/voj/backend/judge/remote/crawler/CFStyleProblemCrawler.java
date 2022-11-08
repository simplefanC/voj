package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import com.simplefanc.voj.common.utils.CodeForcesUtils;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public abstract class CFStyleProblemCrawler extends ProblemCrawler {
    public static final String HOST = "https://codeforces.com";

    protected List<HttpCookie> cookies;
    protected abstract String getProblemUrl(String contestId, String problemNum);

    protected abstract String getProblemSource(String html, String problemId, String contestId, String problemNum);

    @Override
    public ProblemCrawler.RemoteProblemInfo getProblemInfo(String problemId) throws Exception {
        String contestId;
        String problemNum;
        if (NumberUtil.isInteger(problemId)) {
            contestId = ReUtil.get("([0-9]+)[0-9]{2}", problemId, 1);
            problemNum = ReUtil.get("[0-9]+([0-9]{2})", problemId, 1);
        } else {
            contestId = ReUtil.get("([0-9]+)[A-Z]{1}[0-9]{0,1}", problemId, 1);
            problemNum = ReUtil.get("[0-9]+([A-Z]{1}[0-9]{0,1})", problemId, 1);
        }

        if (contestId == null || problemNum == null) {
            throw new IllegalArgumentException("Codeforces: Incorrect problem id format!");
        }

        HttpRequest request = HttpRequest.get(getProblemUrl(contestId, problemNum))
                .header("cookie", "RCPC=" + CodeForcesUtils.getRCPC())
                .timeout(20000);
        if (cookies != null) {
            request.cookie(cookies);
        }
        String html = request
                .execute()
                .body();

        // 重定向失效，更新RCPC
        if (html.contains("Redirecting... Please, wait.")) {
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", html, 0, new ArrayList<>());
            CodeForcesUtils.updateRCPC(list);
            html = request.execute()
                    .body();
        }

        RemoteProblemInfo problemInfo = new RemoteProblemInfo();
        Problem info = problemInfo.getProblem();
        info.setProblemId(getOjInfo() + "-" + problemId);

        info.setTitle(ReUtil.get("<div class=\"title\">\\s*" + problemNum + "\\. ([\\s\\S]*?)</div>", html, 1).trim());

        String timeLimitStr = ReUtil.get("</div>\\s*([\\d\\.]+) (seconds?|s)\\s*</div>", html, 1);
        if (StrUtil.isEmpty(timeLimitStr)) {
            timeLimitStr = ReUtil.get("</div>\\s*<span .*?>(\\d+) (seconds?|s)\\s*</span>\\s*</div>", html, 1);
        }

        double timeLimit = 1000 * Double.parseDouble(timeLimitStr);
        info.setTimeLimit((int) timeLimit);

        String memoryLimitStr = ReUtil.get("</div>\\s*(\\d+) (megabytes|MB)\\s*</div>", html, 1);
        if (StrUtil.isEmpty(memoryLimitStr)) {
            memoryLimitStr = ReUtil.get("</div>\\s*<span .*?>(\\d+) (megabytes|MB)\\s*</span>\\s*</div>", html, 1);
        }

        info.setMemoryLimit(Integer.parseInt(memoryLimitStr));

        String tmpDesc = ReUtil.get("standard output\\s*</div>\\s*</div>\\s*<div>([\\s\\S]*?)</div>\\s*<div class=\"input-specification",
                html, 1);
        if (StrUtil.isEmpty(tmpDesc)) {
            tmpDesc = ReUtil.get("<div class=\"input-file\">([\\s\\S]*?)</div><div class=\"input-specification", html, 1);
        }

        if (StrUtil.isEmpty(tmpDesc)) {
            // 交互题
            tmpDesc = ReUtil.get("standard output\\s*</div>\\s*</div>\\s*<div>([\\s\\S]*?)</div>\\s*<div>\\s*<div class=\"section-title", html, 1);
        }

        if (StrUtil.isEmpty(tmpDesc)) {
            // 单单只有题面描述
            tmpDesc = ReUtil.get("standard output\\s*</div>\\s*</div>\\s*<div>([\\s\\S]*?)</div>",
                    html, 1);
        }

        if (!StrUtil.isEmpty(tmpDesc)) {
            tmpDesc = tmpDesc.replaceAll("\\$\\$\\$", "\\$")
                    .replaceAll("src=\"../../", "src=\"" + HOST + "/")
                    .trim();
        }
        info.setDescription(tmpDesc);

        String inputDesc = ReUtil.get("<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"output-specification\">", html, 1);
        if (StrUtil.isEmpty(inputDesc)) {
            inputDesc = ReUtil.get("<div class=\"section-title\">\\s*Interaction\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"sample-tests\">", html, 1);
        }
        if (StrUtil.isEmpty(inputDesc)) {
            inputDesc = ReUtil.get("<div class=\"input-specification\">\\s*<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div>", html, 1);
        }
        if (!StrUtil.isEmpty(inputDesc)) {
            inputDesc = inputDesc.replaceAll("\\$\\$\\$", "\\$").trim();
        }
        info.setInput(inputDesc);

        String outputDesc = ReUtil.get("<div class=\"section-title\">\\s*Output\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"sample-tests\">", html, 1);
        if (!StrUtil.isEmpty(outputDesc)) {
            outputDesc = outputDesc.replaceAll("\\$\\$\\$", "\\$").trim();
        }
        info.setOutput(outputDesc);

        StringBuilder sb = new StringBuilder();
        List<String> inputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"input\">\\s*<div class=\"title\">\\s*Input\\s*</div>\\s*<pre>([\\s\\S]*?)</pre>\\s*</div>"), html, 1);
        List<String> outputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"output\">\\s*<div class=\"title\">\\s*Output\\s*</div>\\s*<pre>([\\s\\S]*?)</pre>\\s*</div>"), html, 1);
        for (int i = 0; i < inputExampleList.size() && i < outputExampleList.size(); i++) {
            sb.append("<input>");
            String input = inputExampleList.get(i)
                    .replaceAll("<br>", "\n")
                    .replaceAll("<br />", "\n")
                    .replaceAll("<div .*?>", "")
                    .replaceAll("</div>", "\n")
                    .trim();
            sb.append(HtmlUtil.unescape(input)).append("</input>");
            sb.append("<output>");
            String output = outputExampleList.get(i)
                    .replaceAll("<br>", "\n")
                    .replaceAll("<br />", "\n")
                    .trim();
            sb.append(HtmlUtil.unescape(output)).append("</output>");
        }
        info.setExamples(sb.toString());

        String tmpHint = ReUtil.get("<div class=\"section-title\">\\s*Note\\s*</div>([\\s\\S]*?)</div>\\s*</div>", html, 1);
        if (tmpHint != null) {
            info.setHint(tmpHint.replaceAll("\\$\\$\\$", "\\$").trim());
        }

        info.setSource(getProblemSource(html, problemId, contestId, problemNum));

        List<String> allTags = ReUtil.findAll(Pattern.compile("<span class=\"tag-box\" style=\"font-size:1\\.2rem;\" title=\"[\\s\\S]*?\">([\\s\\S]*?)</span>"), html, 1);
        List<Tag> tagList = new LinkedList<>();
        for (String tmp : allTags) {
            tagList.add(new Tag().setName(tmp.trim()));
        }
        return problemInfo.setTagList(tagList);
    }

}