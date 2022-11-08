package com.simplefanc.voj.backend.judge.remote.crawler;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.dao.judge.RemoteJudgeAccountEntityService;
import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.entity.judge.RemoteJudgeAccount;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.utils.CodeForcesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GYMProblemCrawler extends CFStyleProblemCrawler {
    private final FilePathProperties filePathProperties;
    private final RemoteJudgeAccountEntityService remoteJudgeAccountEntityService;

    private static final String PROBLEM_URL = "/gym/%s/problem/%s";
    private static final String LOGIN_URL = "/enter";

    @Override
    public String getOjInfo() {
        return RemoteOj.GYM.getName();
    }

    @Override
    public String getProblemUrl(String contestId, String problemNum) {
        return HOST + String.format(PROBLEM_URL, contestId, problemNum);
    }

    @Override
    public String getProblemSource(String html, String problemId, String contestNum, String problemNum) {
        return String.format("<p>Problem：<a style='color:#1A5CC8' href='https://codeforces.com/gym/%s/problem/%s'>%s</a></p><p>" +
                        "Contest：" + ReUtil.get("(<a[^<>]+/gym/\\d+\">.+?</a>)", html, 1)
                        .replace("/gym", HOST + "/gym")
                        .replace("color: black", "color: #009688;") + "</p>",
                contestNum, problemNum, getOjInfo() + "-" + problemId);
    }

    @Override
    public ProblemCrawler.RemoteProblemInfo getProblemInfo(String problemId) {
        if (cookies == null) {
            RemoteJudgeAccount account = remoteJudgeAccountEntityService.lambdaQuery()
                    .eq(RemoteJudgeAccount::getOj, RemoteOj.CF.getName())
                    .list().get(0);
            if (account != null) {
                login(account.getUsername(), account.getPassword());
            }
        }
        try {
            return super.getProblemInfo(problemId);
        } catch (Exception ignored) {
            String contestNum = ReUtil.get("([0-9]+)[A-Z]{1}[0-9]{0,1}", problemId, 1);
            String problemNum = ReUtil.get("[0-9]+([A-Z]{1}[0-9]{0,1})", problemId, 1);
            return getPDFHtml(problemId, contestNum, problemNum);
        }
    }

    private RemoteProblemInfo getPDFHtml(String problemId, String contestNum, String problemNum) {
        RemoteProblemInfo problemInfo = new RemoteProblemInfo();
        Problem problem = problemInfo.getProblem();

        String url = HOST + "/gym/" + contestNum;
        HttpRequest request = HttpRequest.get(url)
                .header("cookie", "RCPC=" + CodeForcesUtils.getRCPC())
                .timeout(20000);
        if (cookies != null) {
            request.cookie(cookies);
        }
        String html = request.execute().body();

        // 重定向失效，更新RCPC
        if (html.contains("Redirecting... Please, wait.")) {
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", html, 0, new ArrayList<>());
            CodeForcesUtils.updateRCPC(list);
            html = request.execute().body();
        }

        String regex = "<a href=\"/gym/" + contestNum + "/problem/" + problemNum
                + "\"><!--\\s*-->([^<]+)(?:(?:.|\\s)*?<div){2}[^>]*>\\s*([^<]+)</div>\\s*([\\d.]+)\\D*(\\d+)";

        Matcher matcher = Pattern.compile(regex).matcher(html);
        matcher.find();

        problem.setProblemId(getOjInfo() + "-" + problemId);
        problem.setTitle(matcher.group(1));
        problem.setTimeLimit((int) (Double.parseDouble(matcher.group(3)) * 1000));
        problem.setMemoryLimit(Integer.parseInt(matcher.group(4)));

        problem.setSource(String.format("<p>Problem：<a style='color:#1A5CC8' href='https://codeforces.com/gym/%s/attachments'>%s</a></p><p>" +
                        "Contest：" + ReUtil.get("(<a[^<>]+/gym/\\d+\">.+?</a>)", html, 1)
                        .replace("/gym", HOST + "/gym")
                        .replace("color: black", "color: #009688;") + "</p>",
                contestNum, getOjInfo() + "-" + problemId));


        regex = "/gym/" + contestNum + "/attachments/download\\S*?\\.pdf";

        matcher = Pattern.compile(regex).matcher(html);
        matcher.find();

        String pdfURI;
        try {
            String fileName = IdUtil.fastSimpleUUID() + ".pdf";
            String filePath = filePathProperties.getProblemFileFolder() + File.separator + fileName;
            HttpUtil.downloadFile(HOST + matcher.group(0), filePath);
            pdfURI = filePathProperties.getFileApi() + fileName;
        } catch (Exception e1) {
            try {
                pdfURI = HOST + matcher.group(0);
            } catch (Exception e2) {
                String fileName = IdUtil.fastSimpleUUID() + ".pdf";
                String filePath = filePathProperties.getProblemFileFolder() + File.separator + fileName;
                CodeForcesUtils.downloadPDF(HOST + "/gym/" + contestNum + "/problem/" + problemNum, filePath);
                pdfURI = filePathProperties.getFileApi() + fileName;
            }
        }
        String description = "<p><a style='color:#3091f2' href=\"" + pdfURI + "\">Click here to download the PDF file.</a></p>";
        problem.setDescription(description);
        return problemInfo;
    }

    public void login(String username, String password) {
        HashMap<String, Object> keyMap = getCsrfToken(HOST + LOGIN_URL, false);

        HttpRequest httpRequest = new HttpRequest(HOST + LOGIN_URL);
        httpRequest.setConnectionTimeout(60000);
        httpRequest.setReadTimeout(60000);
        httpRequest.setMethod(Method.POST);
        httpRequest.cookie(cookies);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("csrf_token", keyMap.get("csrf_token"));
        hashMap.put("action", "enter");
        hashMap.put("ftaa", keyMap.get("ftaa"));
        hashMap.put("bfaa", keyMap.get("bfaa"));
        hashMap.put("handleOrEmail", username);
        hashMap.put("password", password);
        hashMap.put("remember", "on");
        httpRequest.form(hashMap);
        HttpResponse response = httpRequest.execute();
        cookies = response.getCookies();
    }

    public HashMap<String, Object> getCsrfToken(String url, boolean needTTA) {

        HttpRequest request = HttpUtil.createGet(url);

        request.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());

        HttpResponse response = request.execute();
        String body = response.body();
        if (body.contains("Redirecting... Please, wait.")) {
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", body, 0, new ArrayList<>());
            CodeForcesUtils.updateRCPC(list);
            request.removeHeader("cookie");
            request.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());
            response = request.execute();
            body = response.body();
        }

        HashMap<String, Object> res = new HashMap<>();
        cookies = response.getCookies();
        String ftaa = response.getCookieValue("70a7c28f3de");
        res.put("ftaa", ftaa);

        String bfaa = ReUtil.get("_bfaa = \"(.{32})\"", body, 1);
        if (StringUtils.isEmpty(bfaa)) {
            bfaa = response.getCookieValue("raa");
            if (StringUtils.isEmpty(bfaa)) {
                bfaa = response.getCookieValue("bfaa");
            }
        }
        res.put("bfaa", bfaa);

        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", body, 1);
        res.put("csrf_token", csrfToken);

        if (needTTA) {
            String _39ce7 = response.getCookieValue("39ce7");
            int _tta = 0;
            for (int c = 0; c < _39ce7.length(); c++) {
                _tta = (_tta + (c + 1) * (c + 2) * _39ce7.charAt(c)) % 1009;
                if (c % 3 == 0)
                    _tta++;
                if (c % 2 == 0)
                    _tta *= 2;
                if (c > 0)
                    _tta -= (_39ce7.charAt(c / 2) / 2) * (_tta % 5);
                while (_tta < 0)
                    _tta += 1009;
                while (_tta >= 1009)
                    _tta -= 1009;
            }
            res.put("_tta", _tta);
        }
        return res;
    }
}