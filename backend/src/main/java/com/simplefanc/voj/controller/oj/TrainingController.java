package com.simplefanc.voj.controller.oj;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.*;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.pojo.dto.RegisterTrainingDto;
import com.simplefanc.voj.pojo.vo.AccessVo;
import com.simplefanc.voj.pojo.vo.ProblemVo;
import com.simplefanc.voj.pojo.vo.TrainingRankVo;
import com.simplefanc.voj.pojo.vo.TrainingVo;
import com.simplefanc.voj.service.oj.TrainingService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/11/19 21:42
 * @Description: 处理训练题单的请求
 */

@RestController
@RequestMapping("/api")
public class TrainingController {

    @Resource
    private TrainingService trainingService;

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
    @GetMapping("/get-training-list")
    public CommonResult<IPage<TrainingVo>> getTrainingList(@RequestParam(value = "limit", required = false) Integer limit,
                                                           @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                           @RequestParam(value = "keyword", required = false) String keyword,
                                                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                           @RequestParam(value = "auth", required = false) String auth) {
        return CommonResult.successResponse(trainingService.getTrainingList(limit, currentPage, keyword, categoryId, auth));
    }


    /**
     * @param tid
     * @MethodName getTraining
     * @Description 根据tid获取指定训练详情
     * @Return
     * @Since 2021/11/20
     */
    @GetMapping("/get-training-detail")
    @RequiresAuthentication
    public CommonResult<TrainingVo> getTraining(@RequestParam(value = "tid") Long tid) {
        return CommonResult.successResponse(trainingService.getTraining(tid));
    }

    /**
     * @param tid
     * @MethodName getTrainingProblemList
     * @Description 根据tid获取指定训练的题单题目列表
     * @Return
     * @Since 2021/11/20
     */
    @GetMapping("/get-training-problem-list")
    @RequiresAuthentication
    public CommonResult<List<ProblemVo>> getTrainingProblemList(@RequestParam(value = "tid") Long tid) {
        return CommonResult.successResponse(trainingService.getTrainingProblemList(tid));
    }

    /**
     * @param registerTrainingDto
     * @MethodName toRegisterTraining
     * @Description 注册校验私有权限的训练
     * @Return
     * @Since 2021/11/20
     */
    @PostMapping("/register-training")
    @RequiresAuthentication
    public CommonResult<Void> toRegisterTraining(@RequestBody RegisterTrainingDto registerTrainingDto) {
        trainingService.toRegisterTraining(registerTrainingDto);
        return CommonResult.successResponse();
    }


    /**
     * @param tid
     * @MethodName getTrainingAccess
     * @Description 私有权限的训练需要获取当前用户是否有进入训练的权限
     * @Return
     * @Since 2021/11/20
     */
    @RequiresAuthentication
    @GetMapping("/get-training-access")
    public CommonResult<AccessVo> getTrainingAccess(@RequestParam(value = "tid") Long tid) {
        return CommonResult.successResponse(trainingService.getTrainingAccess(tid));
    }


    /**
     * @param tid
     * @param limit
     * @param currentPage
     * @MethodName getTrainingRank
     * @Description 获取训练的排行榜分页
     * @Return
     * @Since 2021/11/22
     */
    @GetMapping("/get-training-rank")
    @RequiresAuthentication
    public CommonResult<IPage<TrainingRankVo>> getTrainingRank(@RequestParam(value = "tid", required = true) Long tid,
                                                               @RequestParam(value = "limit", required = false) Integer limit,
                                                               @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return CommonResult.successResponse(trainingService.getTrainingRank(tid, limit, currentPage));
    }

}