package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenfan
 * @date 2022/1/17 20:15
 **/
@Component
public class JSKProblemCrawler extends AbstractProblemCrawler {

    public static final String JUDGE_NAME = "JSK";

    public static final String HOST = "https://www.jisuanke.com";

    public static final String PROBLEM_URL = "/problem/%s";

    private Pattern compile1 = Pattern.compile("([\\s\\S]*?)<h4>输入格式</h4><p>([\\s\\S]*?)</p><h4>输出格式</h4><p>([\\s\\S]*?)</p>");
    private Pattern compile2 = Pattern.compile("([\\s\\S]*?)输入格式([\\s\\S]*?)输出格式([\\s\\S]*)");
    private Pattern compile3 = Pattern.compile("([\\s\\S]*?)输入([\\s\\S]*?)输出([\\s\\S]*)");

    /**
     * @param problemId String的原因是因为某些题库题号不是纯数字
     * @return 返回Problem对象
     * @throws Exception
     */
    @Override
    public RemoteProblemInfo getProblemInfo(String problemId) throws Exception {
        // 验证题号是否符合规范 t/a开头+4位数字
        Assert.isTrue(problemId.matches("[TAta]\\d{4}"), "JSK题号格式错误！");
        RemoteProblemInfo problemInfo = new RemoteProblemInfo();
        Problem info = problemInfo.getProblem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        String html = HttpUtil.get(url);
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
            matcher = compile1.matcher(description);
            if (matcher.find()) {
                info.setDescription(matcher.group(1));
                info.setInput(matcher.group(2));
                info.setOutput(matcher.group(3));
            }
        } else {
            matcher = compile2.matcher(description);
            if (matcher.find()) {
                info.setDescription(matcher.group(1));
                info.setInput(matcher.group(2));
                info.setOutput(matcher.group(3));
            } else {
                matcher = compile3.matcher(description);
                if (matcher.find()) {
                    info.setDescription(matcher.group(1));
                    info.setInput(matcher.group(2));
                    info.setOutput(matcher.group(3));
                }
            }
        }

        String sb = "<input>" + jsonObject.getStr("sample_input") +
                "</input><output>" + jsonObject.getStr("sample_output") + "</output>";
        info.setExamples(sb);

        info.setHint(jsonObject.getStr("hint"));
        info.setSource(String.format("<a style='color:#1A5CC8' href='https://www.jisuanke.com/problem/%s'>%s</a>",
                problemId, JUDGE_NAME + "-" + problemId));
        return problemInfo;
    }

    @Override
    public String getOjInfo() {
        return JUDGE_NAME;
    }

}
