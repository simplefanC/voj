package com.simplefanc.voj.backend.service.admin.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.backend.service.admin.training.AdminTrainingCategoryService;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 19:29
 * @Description:
 */

@Service
@RequiredArgsConstructor
public class AdminTrainingCategoryServiceImpl implements AdminTrainingCategoryService {

    private final TrainingCategoryEntityService trainingCategoryEntityService;

    @Override
    public TrainingCategory addTrainingCategory(TrainingCategory trainingCategory) {
        QueryWrapper<TrainingCategory> trainingCategoryQueryWrapper = new QueryWrapper<>();
        trainingCategoryQueryWrapper.eq("name", trainingCategory.getName());
        TrainingCategory existedTrainingCategory = trainingCategoryEntityService.getOne(trainingCategoryQueryWrapper,
                false);

        if (existedTrainingCategory != null) {
            throw new StatusFailException("该分类名称已存在！请勿重复添加！");
        }

        boolean isOk = trainingCategoryEntityService.save(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return trainingCategory;
    }

    @Override
    public void updateTrainingCategory(TrainingCategory trainingCategory) {
        boolean isOk = trainingCategoryEntityService.updateById(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("更新失败！");
        }
    }

    @Override
    public void deleteTrainingCategory(Long cid) {
        boolean isOk = trainingCategoryEntityService.removeById(cid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }

}