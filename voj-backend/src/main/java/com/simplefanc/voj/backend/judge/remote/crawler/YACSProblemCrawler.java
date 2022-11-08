package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author chenfan
 * @date 2022/2/15 17:15
 **/
@Component
public class YACSProblemCrawler extends ProblemCrawler {

    public static final String JUDGE_NAME = "YACS";

    public static final String HOST = "http://www.iai.sh.cn";

    public static final String PROBLEM_URL = "/problem/%s";

    /**
     * @param problemId String的原因是因为某些题库题号不是纯数字
     * @return 返回Problem对象
     * @throws Exception
     */
    @Override
    public RemoteProblemInfo getProblemInfo(String problemId) throws Exception {
        // 验证题号是否符合规范
        Assert.isTrue(problemId.matches("\\d+"), "YACS题号格式错误！");
        RemoteProblemInfo problemInfo = new RemoteProblemInfo();
        Problem info = problemInfo.getProblem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        String html = HttpUtil.get(url);
        html = html.replaceAll("<br>", "\n");
        String problem = ReUtil.getGroup1("\"problem\":({[\\s\\S]*?})}}", html);
        JSONObject jsonObject = JSONUtil.parseObj(problem);
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(jsonObject.getStr("title"));
        info.setTimeLimit(jsonObject.getInt("limitTime"));
        info.setMemoryLimit(jsonObject.getInt("limitMemory"));

        info.setDescription(jsonObject.getStr("description"));
        info.setInput(jsonObject.getStr("inputFormat"));
        info.setOutput(jsonObject.getStr("outputFormat"));

        JSONArray exampleList = jsonObject.getJSONArray("exampleList");

        String sb = "<input>" +
                // .append(jsonObject.getStr("sample_input"))
                "</input><output>" +
                // .append(jsonObject.getStr("sample_output"))
                "</output>";
        info.setExamples(sb);

        info.setHint(jsonObject.getStr("dataRange"));
        info.setSource(String.format("<a style='color:#1A5CC8' href='https://nanti.jisuanke.com/t/%s'>%s</a>",
                problemId, JUDGE_NAME + "-" + problemId));
        return problemInfo;
    }

    @Override
    public String getOjInfo() {
        return JUDGE_NAME;
    }

}
