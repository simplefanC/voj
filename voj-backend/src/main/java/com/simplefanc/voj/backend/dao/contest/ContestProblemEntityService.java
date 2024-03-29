package com.simplefanc.voj.backend.dao.contest;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.ContestProblemVO;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;

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
public interface ContestProblemEntityService extends IService<ContestProblem> {

    List<ContestProblemVO> getContestProblemList(Long cid, Date startTime, Date endTime, Date sealTime, Boolean isAdmin,
                                                 String contestAuthorUid);

    void syncContestRecord(Long pid, Long cid, String displayId);

}
