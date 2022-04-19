package com.simplefanc.voj.server.dao.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.server.pojo.vo.ContestVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
public interface ContestEntityService extends IService<Contest> {

    List<ContestVo> getWithinNext14DaysContests();

    IPage<ContestVo> getContestList(Integer limit, Integer currentPage, Integer type, Integer status, String keyword);

    ContestVo getContestInfoById(long cid);
}
