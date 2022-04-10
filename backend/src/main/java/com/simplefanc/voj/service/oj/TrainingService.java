package com.simplefanc.voj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.RequestParam;
import com.simplefanc.voj.pojo.dto.RegisterTrainingDto;
import com.simplefanc.voj.pojo.entity.training.Training;
import com.simplefanc.voj.pojo.vo.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 17:12
 * @Description:
 */

public interface TrainingService {


    /**
     * @param limit
     * @param currentPage
     * @param keyword
     * @param categoryId
     * @param auth
     * @MethodName getTrainingList
     * @Description 获取训练题单列表，可根据关键词、类别、权限、类型过滤
     * @Return
     * @Since 2021/11/20
     */
    IPage<TrainingVo> getTrainingList(Integer limit, Integer currentPage, String keyword, Long categoryId, String auth);


    /**
     * @param tid
     * @MethodName getTraining
     * @Description 根据tid获取指定训练详情
     * @Return
     * @Since 2021/11/20
     */
    TrainingVo getTraining(@RequestParam(value = "tid") Long tid);

    /**
     * @param tid
     * @MethodName getTrainingProblemList
     * @Description 根据tid获取指定训练的题单题目列表
     * @Return
     * @Since 2021/11/20
     */
    List<ProblemVo> getTrainingProblemList(Long tid);

    /**
     * @param registerTrainingDto
     * @MethodName toRegisterTraining
     * @Description 注册校验私有权限的训练
     * @Return
     * @Since 2021/11/20
     */
    void toRegisterTraining(RegisterTrainingDto registerTrainingDto);


    /**
     * @param tid
     * @MethodName getTrainingAccess
     * @Description 私有权限的训练需要获取当前用户是否有进入训练的权限
     * @Return
     * @Since 2021/11/20
     */
    AccessVo getTrainingAccess(Long tid);


    /**
     * @param tid
     * @param limit
     * @param currentPage
     * @MethodName getTrainingRnk
     * @Description 获取训练的排行榜分页
     * @Return
     * @Since 2021/11/22
     */
    IPage<TrainingRankVo> getTrainingRank(Long tid, Integer limit, Integer currentPage);


    /**
     * 未启用，该操作会导致公开训练也记录，但其实并不需求，会造成数据量无效增加
     */
    void checkAndSyncTrainingRecord(Long pid, Long submitId, String uid);

}