package com.simplefanc.voj.crawler;

import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.utils.JsoupUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenfan
 * @date 2022/1/17 20:15
 **/
@Component
public class JSKProblemCrawler extends ProblemCrawler {
    public static final String JUDGE_NAME = "JSK";
    public static final String HOST = "https://nanti.jisuanke.com";
    public static final String PROBLEM_URL = "/t/%s";

    /**
     * @param problemId String的原因是因为某些题库题号不是纯数字
     * @param author    导入该题目的管理员用户名
     * @return 返回Problem对象
     * @throws Exception
     */
    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        // 验证题号是否符合规范 t/a开头+4位数字
        Assert.isTrue(problemId.matches("[TAta]\\d{4}"), "JSK题号格式错误！");
        Problem info = new Problem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        // 获取连接
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtils.getDocument(connection, null);
        String html = document.html();
        html = html.replaceAll("<br>", "\n");
        String problem = ReUtil.get("var problem=(\\{[\\s\\S]*?\\});", html, 1);
        JSONObject jsonObject = JSONUtil.parseObj(problem);
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setInfo(jsonObject.getStr("id"));
        info.setTitle(jsonObject.getStr("title"));
        info.setTimeLimit(jsonObject.getInt("time_limit"));
        info.setMemoryLimit(jsonObject.getInt("mem_limit") / 1024);

        String description = jsonObject.getStr("description");
        Matcher matcher;
        if (problemId.toUpperCase().charAt(0) == 'T') {
            matcher = Pattern.compile("([\\s\\S]*?)<h4>输入格式</h4><p>([\\s\\S]*?)</p><h4>输出格式</h4><p>([\\s\\S]*?)</p>").matcher(description);
            if (matcher.find()) {
                info.setDescription(matcher.group(1));
                info.setInput(matcher.group(2));
                info.setOutput(matcher.group(3));
            }
        } else {
            matcher = Pattern.compile("([\\s\\S]*?)输入格式([\\s\\S]*?)输出格式([\\s\\S]*)").matcher(description);
            if (matcher.find()) {
                info.setDescription(matcher.group(1));
                info.setInput(matcher.group(2));
                info.setOutput(matcher.group(3));
            } else {
                matcher = Pattern.compile("([\\s\\S]*?)输入([\\s\\S]*?)输出([\\s\\S]*)").matcher(description);
                if (matcher.find()) {
                    info.setDescription(matcher.group(1));
                    info.setInput(matcher.group(2));
                    info.setOutput(matcher.group(3));
                }
            }
        }

        StringBuilder sb = new StringBuilder("<input>")
                .append(jsonObject.getStr("sample_input"))
                .append("</input><output>")
                .append(jsonObject.getStr("sample_output"))
                .append("</output>");
        info.setExamples(sb.toString());

        info.setHint(jsonObject.getStr("hint"));
        info.setIsRemote(true);
        info.setSource(String.format("<a style='color:#1A5CC8' href='https://nanti.jisuanke.com/t/%s'>%s</a>", problemId, JUDGE_NAME + "-" + problemId));
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
