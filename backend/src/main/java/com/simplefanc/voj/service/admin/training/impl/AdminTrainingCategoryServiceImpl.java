package com.simplefanc.voj.service.admin.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.common.exception.StatusFailException;
import com.simplefanc.voj.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.pojo.entity.training.TrainingCategory;
import com.simplefanc.voj.service.admin.training.AdminTrainingCategoryService;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 19:29
 * @Description:
 */

@Service
public class AdminTrainingCategoryServiceImpl implements AdminTrainingCategoryService {

    @Resource
    private TrainingCategoryEntityService trainingCategoryEntityService;

    public TrainingCategory addTrainingCategory(TrainingCategory trainingCategory) {
        QueryWrapper<TrainingCategory> trainingCategoryQueryWrapper = new QueryWrapper<>();
        trainingCategoryQueryWrapper.eq("name", trainingCategory.getName());
        TrainingCategory existedTrainingCategory = trainingCategoryEntityService.getOne(trainingCategoryQueryWrapper, false);

        if (existedTrainingCategory != null) {
            throw new StatusFailException("该分类名称已存在！请勿重复添加！");
        }

        boolean isOk = trainingCategoryEntityService.save(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return trainingCategory;
    }

    public void updateTrainingCategory(TrainingCategory trainingCategory) {
        boolean isOk = trainingCategoryEntityService.updateById(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("更新失败！");
        }
    }

    public void deleteTrainingCategory(Long cid) {
        boolean isOk = trainingCategoryEntityService.removeById(cid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }


}