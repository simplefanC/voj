package com.simplefanc.voj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.pojo.dto.CheckACDto;
import com.simplefanc.voj.pojo.entity.contest.ContestPrint;
import com.simplefanc.voj.pojo.entity.contest.ContestRecord;


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