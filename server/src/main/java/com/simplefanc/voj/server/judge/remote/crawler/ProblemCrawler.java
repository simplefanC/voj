package com.simplefanc.voj.server.judge.remote.crawler;

import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

public abstract class ProblemCrawler {

    public abstract RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception;

    public abstract String getOjInfo();

    @Data
    @Accessors(chain = true)
    public static class RemoteProblemInfo {
        private Problem problem;
        private List<Tag> tagList;
        private List<String> langIdList;
        private RemoteOj remoteOJ;
    }
}
