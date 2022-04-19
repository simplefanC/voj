package com.simplefanc.voj.server.dao.judge;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.server.pojo.vo.JudgeVo;
import com.simplefanc.voj.server.pojo.vo.ProblemCountVo;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */

public interface JudgeEntityService extends IService<Judge> {

    IPage<JudgeVo> getCommonJudgeList(Integer limit,
                                      Integer currentPage,
                                      String searchPid,
                                      Integer status,
                                      String username,
                                      String uid,
                                      Boolean completeProblemID);

    // TODO 参数过多
    IPage<JudgeVo> getContestJudgeList(Integer limit,
                                       Integer currentPage,
                                       String displayId,
                                       Long cid,
                                       Integer status,
                                       String username,
                                       String uid,
                                       Boolean beforeContestSubmit,
                                       String rule,
                                       Date startTime,
                                       Date sealRankTime,
                                       String sealTimeUid,
                                       Boolean completeProblemID);


    void failToUseRedisPublishJudge(Long submitId, Long pid, Boolean isContest);

    ProblemCountVo getContestProblemCount(Long pid,
                                          Long cpid,
                                          Long cid,
                                          Date startTime,
                                          Date sealRankTime,
                                          List<String> adminList);

    ProblemCountVo getProblemCount(Long pid);

    int getTodayJudgeNum();

    List<ProblemCountVo> getProblemListCount(List<Long> pidList);
}
