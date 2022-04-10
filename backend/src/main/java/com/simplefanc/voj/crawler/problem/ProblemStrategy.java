package com.simplefanc.voj.crawler.problem;

import lombok.Data;
import lombok.experimental.Accessors;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.pojo.entity.problem.Tag;
import com.simplefanc.voj.utils.Constants;

import java.util.List;

public abstract class ProblemStrategy {

    public abstract RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception;

    @Data
    @Accessors(chain = true)
    public static
    class RemoteProblemInfo {
        private Problem problem;
        private List<Tag> tagList;
        private List<String> langIdList;
        private Constants.RemoteOJ remoteOJ;
    }
}
