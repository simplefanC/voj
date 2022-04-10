package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.simplefanc.voj.pojo.entity.problem.ProblemCase;

/**
 * @Author: chenfan
 * @Date: 2020/12/14 19:57
 * @Description:
 */
@Mapper
@Repository
public interface ProblemCaseMapper extends BaseMapper<ProblemCase> {
}