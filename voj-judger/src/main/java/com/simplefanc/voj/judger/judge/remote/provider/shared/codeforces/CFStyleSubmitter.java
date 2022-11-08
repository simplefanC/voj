package com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.HttpStatusValidator;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.submitter.Submitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "voj")
public abstract class CFStyleSubmitter implements Submitter {

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    private static final String SUBMISSION_BY_USERNAME = "/submissions/%s";

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        submitCode(client, info, account);
        // 获取提交的题目id
        info.remoteRunId = getMaxIdByParseHtml(client, info, account);
    }

    protected abstract String getSubmitUrl(String contestNum);

    private void submitCode(DedicatedHttpClient client, SubmissionInfo info, RemoteAccount account) {
        processProblemId(info);
        String body = client.get(getSubmitUrl(info.remoteContestId)).getBody();
        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", body, 1);
        account.setCsrfToken(csrfToken);

        HttpPost post = new HttpPost(getSubmitUrl(info.remoteContestId) + "?csrf_token=" + csrfToken);
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "csrf_token", csrfToken,
                "_tta", CFUtil.getTTA(client),
                "bfaa", "",
                "ftaa", "",
                "action", "submitSolutionFormSubmitted",
                "submittedProblemIndex", info.remoteProblemIndex,
                "contestId", info.remoteContestId,
                "programTypeId", getLanguage(info.language),
                "tabsize", "4",
                "source", info.userCode + getRandomBlankString(),
                "sourceCodeConfirmed", "true",
                "doNotShowWarningAgain", "on"
        );
        post.setEntity(entity);
        client.execute(post, HttpStatusValidator.SC_MOVED_TEMPORARILY);
    }

    private void processProblemId(SubmissionInfo info) {
        if (NumberUtil.isInteger(info.remotePid)) {
            info.remoteContestId = ReUtil.get("([0-9]+)[0-9]{2}", info.remotePid, 1);
            // 后两位
            info.remoteProblemIndex = ReUtil.get("[0-9]+([0-9]{2})", info.remotePid, 1);
        } else {// 1238G
            // 1238
            info.remoteContestId = ReUtil.get("([0-9]+)[A-Z]{1}[0-9]{0,1}", info.remotePid, 1);
            // G
            info.remoteProblemIndex = ReUtil.get("[0-9]+([A-Z]{1}[0-9]{0,1})", info.remotePid, 1);
        }
    }

    private String getMaxIdByParseHtml(DedicatedHttpClient client, SubmissionInfo info, RemoteAccount account) {
        final String body = client.get(String.format(SUBMISSION_BY_USERNAME, account.getAccountId())).getBody();
        String maxRunIdStr = ReUtil.get("data-submission-id=\"(\\d+)\"", body, 1);
        if (StrUtil.isEmpty(maxRunIdStr)) {
            log.error("[Codeforces] Failed to parse submission html:{}", body);
            String log = String.format("[Codeforces] Failed to parse html to get run id for problem: [%s]", info.remotePid);
            throw new RuntimeException(log);
        } else {
            return maxRunIdStr;
        }
    }

    private String getRandomBlankString() {
        StringBuilder string = new StringBuilder("\n");
        int random = new Random().nextInt(Integer.MAX_VALUE);
        while (random > 0) {
            string.append(random % 2 == 0 ? ' ' : '\t');
            random /= 2;
        }
        return string.toString();
    }

    private String getLanguage(String language) {
        if (language.startsWith("GNU GCC C11")) {
            return "43";
        } else if (language.startsWith("Clang++17 Diagnostics")) {
            return "52";
        } else if (language.startsWith("GNU G++11")) {
            return "50";
        } else if (language.startsWith("GNU G++14")) {
            return "50";
        } else if (language.startsWith("GNU G++17")) {
            return "54";
        } else if (language.startsWith("GNU G++20")) {
            return "73";
        } else if (language.startsWith("Microsoft Visual C++ 2017")) {
            return "59";
        } else if (language.startsWith("C# 8, .NET Core")) {
            return "65";
        } else if (language.startsWith("C# Mono")) {
            return "9";
        } else if (language.startsWith("D DMD32")) {
            return "28";
        } else if (language.startsWith("Go")) {
            return "32";
        } else if (language.startsWith("Haskell GHC")) {
            return "12";
        } else if (language.startsWith("Java 11")) {
            return "60";
        } else if (language.startsWith("Java 1.8")) {
            return "36";
        } else if (language.startsWith("Kotlin")) {
            return "48";
        } else if (language.startsWith("OCaml")) {
            return "19";
        } else if (language.startsWith("Delphi")) {
            return "3";
        } else if (language.startsWith("Free Pascal")) {
            return "4";
        } else if (language.startsWith("PascalABC.NET")) {
            return "51";
        } else if (language.startsWith("Perl")) {
            return "13";
        } else if (language.startsWith("PHP")) {
            return "6";
        } else if (language.startsWith("Python 2")) {
            return "7";
        } else if (language.startsWith("Python 3")) {
            return "31";
        } else if (language.startsWith("PyPy 2")) {
            return "40";
        } else if (language.startsWith("PyPy 3")) {
            return "41";
        } else if (language.startsWith("Ruby")) {
            return "67";
        } else if (language.startsWith("Rust")) {
            return "49";
        } else if (language.startsWith("Scala")) {
            return "20";
        } else if (language.startsWith("JavaScript")) {
            return "34";
        } else if (language.startsWith("Node.js")) {
            return "55";
        } else {
            return null;
        }
    }
}
