package com.simplefanc.voj.backend.dao.training;


import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.training.TrainingRegister;

import java.util.List;

public interface TrainingRegisterEntityService extends IService<TrainingRegister> {


    List<String> getAlreadyRegisterUidList(Long tid);

}
