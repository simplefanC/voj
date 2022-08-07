package com.simplefanc.voj.judger.judge.remote.provider.atcoder;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.simplefanc.voj.judger.judge.remote.pojo.RemoteOjInfo;
import com.simplefanc.voj.judger.judge.remote.pojo.SubmissionInfo;
import com.simplefanc.voj.judger.judge.remote.account.RemoteAccount;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClientFactory;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleHttpResponse;
import com.simplefanc.voj.judger.judge.remote.httpclient.SimpleNameValueEntityFactory;
import com.simplefanc.voj.judger.judge.remote.submitter.Submitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "voj")
public class AtCoderSubmitter implements Submitter {

    public static final String SUBMIT_URL = "/contests/%s/submit";

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>() {
        {
            put("C (GCC 9.2.1)", "4001");
            put("C (Clang 10.0.0)", "4002");
            put("C++ (GCC 9.2.1)", "4003");
            put("C++ (Clang 10.0.0)", "4004");
            put("Java (OpenJDK 11.0.6)", "4005");
            put("Python (3.8.2)", "4006");
            put("Bash (5.0.11)", "4007");
            put("bc (1.07.1)", "4008");
            put("Awk (GNU Awk 4.1.4)", "4009");
            put("C# (.NET Core 3.1.201)", "4010");
            put("C# (Mono-mcs 6.8.0.105)", "4011");
            put("C# (Mono-csc 3.5.0)", "4012");
            put("Clojure (1.10.1.536)", "4013");
            put("Crystal (0.33.0)", "4014");
            put("D (DMD 2.091.0)", "4015");
            put("D (GDC 9.2.1)", "4016");
            put("D (LDC 1.20.1)", "4017");
            put("Dart (2.7.2)", "4018");
            put("dc (1.4.1)", "4019");
            put("Erlang (22.3)", "4020");
            put("Elixir (1.10.2)", "4021");
            put("F# (.NET Core 3.1.201)", "4022");
            put("F# (Mono 10.2.3)", "4023");
            put("Forth (gforth 0.7.3)", "4024");
            put("Fortran (GNU Fortran 9.2.1)", "4025");
            put("Go (1.14.1)", "4026");
            put("Haskell (GHC 8.8.3)", "4027");
            put("Haxe (4.0.3); js", "4028");
            put("Haxe (4.0.3); Java", "4029");
            put("JavaScript (Node.js 12.16.1)", "4030");
            put("Julia (1.4.0)", "4031");
            put("Kotlin (1.3.71)", "4032");
            put("Lua (Lua 5.3.5)", "4033");
            put("Lua (LuaJIT 2.1.0)", "4034");
            put("Dash (0.5.8)", "4035");
            put("Nim (1.0.6)", "4036");
            put("Objective-C (Clang 10.0.0)", "4037");
            put("Common Lisp (SBCL 2.0.3)", "4038");
            put("OCaml (4.10.0)", "4039");
            put("Octave (5.2.0)", "4040");
            put("Pascal (FPC 3.0.4)", "4041");
            put("Perl (5.26.1)", "4042");
            put("Raku (Rakudo 2020.02.1)", "4043");
            put("PHP (7.4.4)", "4044");
            put("Prolog (SWI-Prolog 8.0.3)", "4045");
            put("PyPy2 (7.3.0)", "4046");
            put("PyPy3 (7.3.0)", "4047");
            put("Racket (7.6)", "4048");
            put("Ruby (2.7.1)", "4049");
            put("Rust (1.42.0)", "4050");
            put("Scala (2.13.1)", "4051");
            put("Java (OpenJDK 1.8.0)", "4052");
            put("Scheme (Gauche 0.9.9)", "4053");
            put("Standard ML (MLton 20130715)", "4054");
            put("Swift (5.2.1)", "4055");
            put("Text (cat 8.28)", "4056");
            put("TypeScript (3.8)", "4057");
            put("Visual Basic (.NET Core 3.1.101)", "4058");
            put("Zsh (5.4.2)", "4059");
            put("COBOL - Fixed (OpenCOBOL 1.1.0)", "4060");
            put("COBOL - Free (OpenCOBOL 1.1.0)", "4061");
            put("Brainfuck (bf 20041219)", "4062");
            put("Ada2012 (GNAT 9.2.1)", "4063");
            put("Unlambda (2.0.0)", "4064");
            put("Cython (0.29.16)", "4065");
            put("Sed (4.4)", "4066");
            put("Vim (8.2.0460)", "4067");
        }
    };

    private final DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return AtCoderInfo.INFO;
    }

    protected String getRunId(DedicatedHttpClient client, SubmissionInfo info, String username) {
        String body = client.get(String.format("/contests/%s/submissions?f.Task=%s&f.User=%s", info.remoteContestId, info.remotePid, username)).getBody();
        return ReUtil.get("<a href=\"/contests/" + info.remoteContestId + "/submissions/(\\d+)\">Detail</a>", body, 1);
    }

    private String getRunId(SubmissionInfo info, String username) {
        HttpRequest.getCookieManager().getCookieStore().removeAll();
        String url = getOjInfo().mainHost + String.format("/contests/%s/submissions?f.Task=%s&f.User=%s", info.remoteContestId, info.remotePid, username);
        String body = HttpUtil.get(url);
        return ReUtil.get("<a href=\"/contests/" + info.remoteContestId + "/submissions/(\\d+)\">Detail</a>", body, 1);
    }

    private HttpResponse trySubmit(SubmissionInfo info, RemoteAccount account) {
        String submitUrl = getOjInfo().mainHost + String.format(SUBMIT_URL, info.remoteContestId);
        HttpRequest request = HttpUtil.createPost(submitUrl);
        HttpRequest httpRequest = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("data.TaskScreenName", info.remotePid)
                .put("data.LanguageId", LANGUAGE_MAP.get(info.language))
                .put("sourceCode", info.userCode)
                .put("csrf_token", account.getCsrfToken()).map());
        httpRequest.cookie(account.getCookies());
        return httpRequest.execute();
    }

    @Override
    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
//        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        String[] arr = info.remotePid.split("_");
        info.remoteContestId = arr[0];
        info.remoteProblemIndex = arr[1];
        HttpResponse response = trySubmit(info, account);
        // 说明被限制提交频率了
        if (response.getStatus() == HttpStatus.SC_OK) {
            String timeStr = ReUtil.get("Wait for (\\d+) second to submit again.", response.body(), 1);
            if (timeStr != null) {
                int time = Integer.parseInt(timeStr);
                try {
                    TimeUnit.SECONDS.sleep(time + 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                response = trySubmit(info, account);
            }
        }

        if (response.getStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
            log.error("Submit to AtCoder failed, the response status:{}, It may be that the frequency of submission operation is too fast. Please try later", response.getStatus());
            throw new RuntimeException("[AtCoder] Failed to Submit, the response status:" + response.getStatus());
        }

        // 停留3秒钟后再获取id，之后归还账号，避免提交频率过快
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        info.remoteRunId = getRunId(client, info, account.getAccountId());
        info.remoteRunId = getRunId(info, account.getAccountId());
    }

//    @Override
//    public void submit(SubmissionInfo info, RemoteAccount account) throws Exception {
//        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
//        String[] arr = info.remotePid.split("_");
//        info.remoteContestId = arr[0];
//        info.remoteProblemIndex = arr[1];
//
//        SimpleHttpResponse response = trySubmit(client, info, account);
//        // 说明被限制提交频率了
//        if (response.getStatusCode() == HttpStatus.SC_OK) {
//            String timeStr = ReUtil.get("Wait for (\\d+) second to submit again.", response.getBody(), 1);
//            if (timeStr != null) {
//                int time = Integer.parseInt(timeStr);
//                try {
//                    TimeUnit.SECONDS.sleep(time + 1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                response = trySubmit(client, info, account);
//            }
//        }
//
//        if (response.getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY) {
//            log.error("Submit to AtCoder failed, the response status:{}, It may be that the frequency of submission operation is too fast. Please try later", response.getStatusCode());
//            throw new RuntimeException("[AtCoder] Failed to Submit, the response status:" + response.getStatusCode());
//        }
//
//        // 停留3秒钟后再获取id，之后归还账号，避免提交频率过快
//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        info.remoteRunId = getRunId(client, info, account.getAccountId());
//    }

    @Deprecated
    private SimpleHttpResponse trySubmit(DedicatedHttpClient client, SubmissionInfo info, RemoteAccount account) {
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "data.TaskScreenName", info.remotePid,
                "data.LanguageId", LANGUAGE_MAP.get(info.language),
                "sourceCode", info.userCode,
                "csrf_token", account.getCsrfToken()
        );
        return client.post(String.format(SUBMIT_URL, info.remoteContestId), entity);
//        HttpPost post = new HttpPost(String.format("/contests/%s/submit", contestId));
//        post.setEntity(entity);
//        client.execute(post, new ResponseHandler<Object>() {
//            @Override
//            public Object handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
//                return null;
//            }
//        });
    }

}
