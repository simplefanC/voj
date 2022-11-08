package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: chenfan
 * @Date: 2021/7/26 9:00
 * @Description:
 */
@Component
public class AtCoderProblemCrawler extends ProblemCrawler {

    public static final String HOST = "https://atcoder.jp";
    public static final String PROBLEM_URL = "/contests/%s/tasks/%s";

    public String getProblemUrl(String problemId, String contestId) {
        return HOST + String.format(PROBLEM_URL, contestId, problemId);
    }

    public String getProblemSource(String problemId, String contestId) {
        return String.format("<a style='color:#1A5CC8' href='" + getProblemUrl(problemId, contestId) + "'>%s</a>", "AtCoder-" + problemId);
    }

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId) throws Exception {
        problemId = problemId.toLowerCase();
        boolean isMatch = ReUtil.isMatch("[a-z]+[0-9]+_[a-z]*[0-9]*", problemId);
        if (!isMatch) {
            throw new IllegalArgumentException("AtCoder: Incorrect problem id format! Must be like `abc110_a`");
        }

        String contestId = problemId.split("_")[0];

        String body = HttpUtil.get(getProblemUrl(problemId, contestId));
        Pattern pattern = Pattern.compile("Time Limit: (\\d+) sec / Memory Limit: (\\d+) MB");
        Matcher matcher = pattern.matcher(body);
        Assert.isTrue(matcher.find());
        String timeLimit = matcher.group(1).trim();
        String memoryLimit = matcher.group(2).trim();
        String title = ReUtil.get("<title>[\\s\\S]*? - ([\\s\\S]*?)</title>", body, 1);


        Problem problem = new Problem();
        problem.setProblemId(getOjInfo() + "-" + problemId)
                .setTitle(title)
                .setTimeLimit(Integer.parseInt(timeLimit) * 1000)
                .setMemoryLimit(Integer.parseInt(memoryLimit))
                .setSource(getProblemSource(problemId, contestId));

        if (body.contains("Problem Statement")) {
            String desc = ReUtil.get("<h3>Problem Statement</h3>([\\s\\S]*?)</section>[\\s\\S]*?</div>", body, 1);

            desc = desc.replaceAll("<var>", "\\$").replaceAll("</var>", "\\$");
            desc = desc.replaceAll("<pre>", "<pre style=\"padding:9px!important;background-color: #f5f5f5!important\">");
            desc = desc.replaceAll("src=\"/img", "src=\"" + HOST + "/img");

            StringBuilder sb = new StringBuilder();
            String rawInput = ReUtil.get("<h3>Input</h3>([\\s\\S]*?)</section>[\\s\\S]*?</div>", body, 1);
            sb.append(rawInput);
            String constrains = ReUtil.get("<h3>Constraints</h3>([\\s\\S]*?)</section>[\\s\\S]*?</div>", body, 1);
            sb.append(constrains);
            String input = sb.toString().replaceAll("<var>", "\\$").replaceAll("</var>", "\\$");
            input = input.replaceAll("<pre>", "<pre style=\"padding:9px!important;background-color: #f5f5f5!important\">");


            String rawOutput = ReUtil.get("<h3>Output</h3>([\\s\\S]*?)</section>[\\s\\S]*?</div>", body, 1);
            String output = rawOutput.replaceAll("<var>", "\\$").replaceAll("</var>", "\\$");
            output = output.replaceAll("<pre>", "<pre style=\"padding:9px!important;background-color: #f5f5f5!important\">");

            List<String> sampleInput = ReUtil.findAll("<h3>Sample Input \\d+</h3><pre>([\\s\\S]*?)</pre>[\\s\\S]*?</section>[\\s\\S]*?</div>", body, 1);
            List<String> sampleOutput = ReUtil.findAll("<h3>Sample Output \\d+</h3><pre>([\\s\\S]*?)</pre>[\\s\\S]*?</section>[\\s\\S]*?</div>", body, 1);


            StringBuilder examples = new StringBuilder();
            for (int i = 0; i < sampleInput.size() && i < sampleOutput.size(); i++) {
                examples.append("<input>");
                String exampleInput = sampleInput.get(i).trim();
                examples.append(exampleInput).append("</input>");
                examples.append("<output>");
                String exampleOutput = sampleOutput.get(i).trim();
                examples.append(exampleOutput).append("</output>");
            }

            problem.setInput(input.trim())
                    .setOutput(output.trim())
                    .setDescription(desc.trim())
                    .setExamples(examples.toString());


        } else {
            Element element = Jsoup.parse(body).getElementById("task-statement");
            String desc = element.html();
            desc = desc.replaceAll("src=\"/img", "src=\"https://atcoder.jp/img");
            desc = desc.replaceAll("<pre>", "<pre style=\"padding:9px!important;background-color: #f5f5f5!important\">");
            desc = desc.replaceAll("<var>", "\\$").replaceAll("</var>", "\\$");
            desc = desc.replaceAll("<hr>", "");
            problem.setDescription(desc);
        }
        return new RemoteProblemInfo()
                .setProblem(problem);
    }

    @Override
    public String getOjInfo() {
        return RemoteOj.AtCoder.getName();
    }


}