package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.pojo.entity.training.TrainingCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TrainingCategoryMapper extends BaseMapper<TrainingCategory> {

    public TrainingCategory getTrainingCategoryByTrainingId(@Param("tid") Long tid);
}
