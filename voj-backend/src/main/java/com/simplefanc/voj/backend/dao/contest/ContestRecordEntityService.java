package com.simplefanc.voj.backend.dao.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.ContestRecordVo;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
public interface ContestRecordEntityService extends IService<ContestRecord> {

    IPage<ContestRecord> getACInfo(Integer currentPage, Integer limit, Integer status, Long cid,
                                   String contestCreatorId);

    List<ContestRecordVo> getOIContestRecord(Contest contest, Boolean isOpenSealRank);

    List<ContestRecordVo> getACMContestRecord(String username, Long cid);

}
