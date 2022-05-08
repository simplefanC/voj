package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainingCategoryMapper extends BaseMapper<TrainingCategory> {

    public TrainingCategory getTrainingCategoryByTrainingId(@Param("tid") Long tid);

}
