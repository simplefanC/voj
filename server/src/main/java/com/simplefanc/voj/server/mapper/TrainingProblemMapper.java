package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.server.pojo.vo.ProblemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TrainingProblemMapper extends BaseMapper<TrainingProblem> {

    public List<Long> getTrainingProblemCount(@Param("tid") Long tid);

    public List<ProblemVo> getTrainingProblemList(@Param("tid") Long tid);

}
