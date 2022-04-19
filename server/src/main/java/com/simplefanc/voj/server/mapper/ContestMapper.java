package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.server.pojo.vo.ContestRegisterCountVo;
import com.simplefanc.voj.server.pojo.vo.ContestVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface ContestMapper extends BaseMapper<Contest> {
    List<ContestVo> getContestList(IPage page, @Param("type") Integer type,
                                   @Param("status") Integer status, @Param("keyword") String keyword);

    List<ContestRegisterCountVo> getContestRegisterCount(@Param("cidList") List<Long> cidList);

    ContestVo getContestInfoById(@Param("cid") long cid);

    List<ContestVo> getWithinNext14DaysContests();
}
