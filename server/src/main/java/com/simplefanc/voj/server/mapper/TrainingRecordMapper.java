package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.training.TrainingRecord;
import com.simplefanc.voj.server.pojo.vo.TrainingRecordVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/11/21 14:27
 * @Description:
 */

@Mapper
@Repository
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

    public List<TrainingRecordVo> getTrainingRecord(@Param("tid") Long tid);
}