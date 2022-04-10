package com.simplefanc.voj.service.oj;

import com.simplefanc.voj.pojo.dto.ContestRankDto;
import com.simplefanc.voj.pojo.vo.ContestOutsideInfo;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 20:02
 * @Description:
 */

public interface ContestScoreboardService {

    ContestOutsideInfo getContestOutsideInfo(Long cid);


    List getContestOutsideScoreboard(ContestRankDto contestRankDto);
}