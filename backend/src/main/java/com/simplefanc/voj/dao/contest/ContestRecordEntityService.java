package com.simplefanc.voj.dao.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.contest.Contest;
import com.simplefanc.voj.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.pojo.vo.ContestRecordVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
public interface ContestRecordEntityService extends IService<ContestRecord> {

    IPage<ContestRecord> getACInfo(Integer currentPage,
                                   Integer limit,
                                   Integer status,
                                   Long cid,
                                   String contestCreatorId);

    List<ContestRecordVo> getOIContestRecord(Contest contest, Boolean isOpenSealRank);

    List<ContestRecordVo> getACMContestRecord(String username, Long cid);

}
