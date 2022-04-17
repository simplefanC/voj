package com.simplefanc.voj.dao.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.training.TrainingRegisterEntityService;
import com.simplefanc.voj.mapper.TrainingRegisterMapper;
import com.simplefanc.voj.pojo.entity.training.TrainingRegister;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2021/11/20 11:30
 * @Description:
 */
@Service
public class TrainingRegisterEntityServiceImpl extends ServiceImpl<TrainingRegisterMapper, TrainingRegister> implements TrainingRegisterEntityService {

    @Resource
    private TrainingRegisterMapper trainingRegisterMapper;


    @Override
    public List<String> getAlreadyRegisterUidList(Long tid) {
        QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
        trainingRegisterQueryWrapper.eq("tid", tid);
        return trainingRegisterMapper.selectList(trainingRegisterQueryWrapper).stream().map(TrainingRegister::getUid).collect(Collectors.toList());
    }

}