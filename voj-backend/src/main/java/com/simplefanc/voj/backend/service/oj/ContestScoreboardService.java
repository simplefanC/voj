package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.ContestRankDTO;
import com.simplefanc.voj.backend.pojo.vo.ContestOutsideInfo;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 20:02
 * @Description:
 */

public interface ContestScoreboardService {

    ContestOutsideInfo getContestOutsideInfo(Long cid);

    IPage getContestOutsideScoreboard(ContestRankDTO contestRankDTO);

}