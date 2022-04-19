package com.simplefanc.voj.judger.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author chenfan
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface ProblemMapper extends BaseMapper<Problem> {

}
