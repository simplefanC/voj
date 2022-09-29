package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.ProblemVo;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {

    List<ProblemVo> getProblemList(IPage page, @Param("keyword") String keyword,
                                   @Param("difficulty") Integer difficulty, @Param("tid") List<Long> tid,
                                   @Param("tagListSize") Integer tagListSize, @Param("oj") String oj, @Param("allProblemVisible") boolean allProblemVisible);

}
