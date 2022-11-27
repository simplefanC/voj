package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.backend.pojo.vo.ContestRecordVO;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
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
public interface ContestRecordMapper extends BaseMapper<ContestRecord> {

    List<ContestRecord> getACInfo(@Param("status") Integer status, @Param("cid") Long cid);

    List<ContestRecordVO> getOIContestRecordByRecentSubmission(@Param("cid") Long cid,
                                                               @Param("isOpenSealRank") Boolean isOpenSealRank,
                                                               @Param("sealTime") Date sealTime, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<ContestRecordVO> getOIContestRecordByHighestSubmission(@Param("cid") Long cid,
                                                                @Param("isOpenSealRank") Boolean isOpenSealRank,
                                                                @Param("sealTime") Date sealTime, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<ContestRecordVO> getACMContestRecord(@Param("cid") Long cid, @Param("startTime") Date startTime);

}
