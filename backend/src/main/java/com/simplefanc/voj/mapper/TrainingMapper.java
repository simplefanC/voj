package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.simplefanc.voj.pojo.entity.training.Training;
import com.simplefanc.voj.pojo.vo.TrainingVo;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/11/19 22:03
 * @Description:
 */
@Mapper
@Repository
public interface TrainingMapper extends BaseMapper<Training> {

    List<TrainingVo> getTrainingList(@Param("categoryId") Long categoryId,
                                     @Param("auth") String auth,
                                     @Param("keyword") String keyword);
}