package com.simplefanc.voj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.training.TrainingRecord;
import com.simplefanc.voj.pojo.vo.TrainingRecordVo;

import java.util.List;


/**
 * @Author: chenfan
 * @Date: 2021/11/21 23:38
 * @Description:
 */
public interface TrainingRecordEntityService extends IService<TrainingRecord> {

    public List<TrainingRecordVo> getTrainingRecord(Long tid);

}