package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.pojo.entity.contest.ContestPrint;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.backend.pojo.dto.CheckACDto;


/**
 * @Author: chenfan
 * @Date: 2022/3/11 19:40
 * @Description:
 */

public interface ContestAdminService {

    IPage<ContestRecord> getContestACInfo(Long cid, Integer currentPage, Integer limit);

    void checkContestACInfo(CheckACDto checkACDto);

    IPage<ContestPrint> getContestPrint(Long cid, Integer currentPage, Integer limit);

    void checkContestPrintStatus(Long id, Long cid);

}