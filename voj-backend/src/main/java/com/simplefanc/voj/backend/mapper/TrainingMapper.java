package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.backend.pojo.vo.TrainingVO;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/11/19 22:03
 * @Description:
 */
@Mapper
public interface TrainingMapper extends BaseMapper<Training> {

    List<TrainingVO> getTrainingList(@Param("categoryId") Long categoryId, @Param("auth") String auth,
                                     @Param("keyword") String keyword);

}