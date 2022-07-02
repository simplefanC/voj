package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.pojo.vo.JudgeVo;
import com.simplefanc.voj.backend.pojo.vo.ProblemCountVo;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Mapper
public interface JudgeMapper extends BaseMapper<Judge> {

    IPage<JudgeVo> getCommonJudgeList(Page<JudgeVo> page, @Param("searchPid") String searchPid,
                                      @Param("status") Integer status, @Param("username") String username, @Param("uid") String uid,
                                      @Param("completeProblemId") Boolean completeProblemId);

    // TODO 参数过多
    IPage<JudgeVo> getContestJudgeList(Page<JudgeVo> page, @Param("displayId") String displayId, @Param("cid") Long cid,
                                       @Param("status") Integer status, @Param("username") String username, @Param("uid") String uid,
                                       @Param("beforeContestSubmit") Boolean beforeContestSubmit, @Param("rule") String rule,
                                       @Param("startTime") Date startTime, @Param("sealRankTime") Date sealRankTime,
                                       @Param("sealTimeUid") String sealTimeUid, @Param("completeProblemId") Boolean completeProblemId);

    int getTodayJudgeNum();

    ProblemCountVo getContestProblemCount(@Param("pid") Long pid, @Param("cpid") Long cpid, @Param("cid") Long cid,
                                          @Param("startTime") Date startTime, @Param("sealRankTime") Date sealRankTime,
                                          @Param("adminList") List<String> adminList);

    ProblemCountVo getProblemCount(@Param("pid") Long pid);

    List<ProblemCountVo> getProblemListCount(@Param("pidList") List<Long> pidList);

}
