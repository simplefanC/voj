package com.simplefanc.voj.backend.dao.judge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.JudgeVO;
import com.simplefanc.voj.backend.pojo.vo.ProblemCountVO;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */

public interface JudgeEntityService extends IService<Judge> {

    IPage<JudgeVO> getCommonJudgeList(Integer limit, Integer currentPage, String searchPid, Integer status,
                                      String username, String uid, Boolean completeProblemId);

    // TODO 参数过多
    IPage<JudgeVO> getContestJudgeList(Integer limit, Integer currentPage, String displayId, Long cid, Integer status,
                                       String username, String uid, Boolean beforeContestSubmit, String rule, Date startTime, Date sealRankTime,
                                       String sealTimeUid, Boolean completeProblemId);

    void failToUseRedisPublishJudge(Long submitId, Long pid, Boolean isContest);

    ProblemCountVO getContestProblemCount(Long pid, Long cpid, Long cid, Date startTime, Date sealRankTime,
                                          List<String> adminList);

    ProblemCountVO getProblemCount(Long pid);

    int getTodayJudgeNum();

    List<ProblemCountVO> getProblemListCount(List<Long> pidList);

}
