package com.simplefanc.voj.backend.judge.remote.crawler;

import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.constants.ProblemEnum;
import com.simplefanc.voj.common.constants.ProblemLevelEnum;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

public abstract class AbstractProblemCrawler {

    public abstract RemoteProblemInfo getProblemInfo(String problemId) throws Exception;

    public abstract String getOjInfo();

    @Data
    @Accessors(chain = true)
    public static class RemoteProblemInfo {
        private Problem problem = Problem.builder()
                .isRemote(true)
                .type(ContestEnum.TYPE_ACM.getCode())
                .auth(ProblemEnum.AUTH_PUBLIC.getCode())
                .author(UserSessionUtil.getUserInfo().getUsername())
                .openCaseResult(false)
                .isRemoveEndBlank(false)
                .difficulty(ProblemLevelEnum.PROBLEM_LEVEL_MID.getCode())
                .build();

        private List<Tag> tagList = null;
    }

}
