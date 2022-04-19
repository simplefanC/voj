package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author: chenfan
 * @Date: 2020/12/14 19:57
 * @Description:
 */
@Mapper
@Repository
public interface ProblemCaseMapper extends BaseMapper<ProblemCase> {
}