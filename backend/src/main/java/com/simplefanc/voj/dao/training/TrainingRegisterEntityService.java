package com.simplefanc.voj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.training.TrainingRegister;

import java.util.List;

public interface TrainingRegisterEntityService extends IService<TrainingRegister> {


    public List<String> getAlreadyRegisterUidList(Long tid);

}
