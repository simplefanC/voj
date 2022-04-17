package com.simplefanc.voj.remoteJudge.provider.jsk;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.remoteJudge.RemoteOjInfo;
import com.simplefanc.voj.remoteJudge.SubmissionInfo;
import com.simplefanc.voj.remoteJudge.SubmissionRemoteStatus;
import com.simplefanc.voj.remoteJudge.account.RemoteAccount;
import com.simplefanc.voj.remoteJudge.httpclient.*;
import com.simplefanc.voj.remoteJudge.querier.Querier;
import com.simplefanc.voj.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JSKQuerier implements Querier {

    /**
     * 统计计蒜客的所有静态原生状态 --> unkown error 暂时未统计
     */
    private static final Map<String, Constants.Judge> statusMap = new HashMap<String, Constants.Judge>() {{
        put("AC", Constants.Judge.STATUS_ACCEPTED);
        put("PE", Constants.Judge.STATUS_PRESENTATION_ERROR);
        put("WA", Constants.Judge.STATUS_WRONG_ANSWER);
        put("TL", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        put("ML", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
        put("OL", Constants.Judge.STATUS_OUTPUT_LIMIT_EXCEEDED);
        put("RE", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("SF", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("AE", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("CE", Constants.Judge.STATUS_COMPILE_ERROR);
        put("pending", Constants.Judge.STATUS_JUDGING);
        put("fail", Constants.Judge.STATUS_SUBMITTED_FAILED);
    }};
    @Autowired
    private DedicatedHttpClientFactory dedicatedHttpClientFactory;

    @Override
    public RemoteOjInfo getOjInfo() {
        return JSKInfo.INFO;
    }

    @Override
    public SubmissionRemoteStatus query(SubmissionInfo info, RemoteAccount account) {
        DedicatedHttpClient client = dedicatedHttpClientFactory.build(getOjInfo().mainHost, account.getContext());
        HttpEntity entity = SimpleNameValueEntityFactory.create(
                "id", info.remotePid
        );
        HttpPost post = new HttpPost("/solve/check/" + info.remoteRunId);
        post.setEntity(entity);
        post.setHeader("X-XSRF-TOKEN", CookieUtil.getCookieValue(client, "XSRF-TOKEN"));
        String body = client.execute(post, HttpStatusValidator.SC_OK).getBody();
        JSONObject jsonObject = JSONUtil.parseObj(body);

        SubmissionRemoteStatus status = new SubmissionRemoteStatus();
        if ("finished".equals(jsonObject.getStr("status"))) {
            // AC
            status.rawStatus = ((JSONObject) jsonObject.get("data")).getStr("reason");
        } else {
            status.rawStatus = "pending";
        }
        status.statusType = statusMap.get(status.rawStatus);
        if (status.statusType == Constants.Judge.STATUS_ACCEPTED) {
            // 执行时间和内存
            HttpGet get = new HttpGet("/t/" + info.remotePid + "/submissions");
            String result = client.execute(get, HttpStatusValidator.SC_OK).getBody();
            JSONObject data = (JSONObject) ((JSONArray) JSONUtil.parseObj(result).get("data")).get(0);
            status.executionMemory = data.getInt("used_mem");
            status.executionTime = data.getInt("used_time");
        } else if (status.statusType == Constants.Judge.STATUS_COMPILE_ERROR) {
            // 编译错误信息
            status.compilationErrorInfo = ((JSONObject) jsonObject.get("data")).getStr("message");
        }
        return status;
    }

}
