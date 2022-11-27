package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.training.*;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.pojo.dto.RegisterTrainingDTO;
import com.simplefanc.voj.backend.pojo.vo.*;
import com.simplefanc.voj.backend.service.admin.training.AdminTrainingRecordService;
import com.simplefanc.voj.backend.service.oj.TrainingService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.backend.validator.TrainingValidator;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.training.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 17:12
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingEntityService trainingEntityService;

    private final TrainingRegisterEntityService trainingRegisterEntityService;

    private final TrainingCategoryEntityService trainingCategoryEntityService;

    private final TrainingProblemEntityService trainingProblemEntityService;

    private final TrainingRecordEntityService trainingRecordEntityService;

    private final UserInfoEntityService userInfoEntityService;

    private final AdminTrainingRecordService adminTrainingRecordService;

    private final TrainingValidator trainingValidator;

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
    @Override
    public IPage<TrainingVO> getTrainingList(Integer limit, Integer currentPage, String keyword, Long categoryId,
                                             String auth) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 30;
        }
        return trainingEntityService.getTrainingList(limit, currentPage, categoryId, auth, keyword);
    }

    /**
     * @param tid
     * @MethodName getTraining
     * @Description 根据tid获取指定训练详情
     * @Return
     * @Since 2021/11/20
     */
    @Override
    public TrainingVO getTraining(Long tid) {
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        boolean isRoot = UserSessionUtil.isRoot();

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        TrainingVO trainingVO = BeanUtil.copyProperties(training, TrainingVO.class);
        TrainingCategory trainingCategory = trainingCategoryEntityService
                .getTrainingCategoryByTrainingId(training.getId());
        trainingVO.setCategoryName(trainingCategory.getName());
        trainingVO.setCategoryColor(trainingCategory.getColor());
        List<Long> trainingProblemIdList = trainingProblemEntityService.getTrainingProblemIdList(training.getId());
        trainingVO.setProblemCount(trainingProblemIdList.size());

        if (userRolesVO != null && trainingValidator.isInTrainingOrAdmin(training, userRolesVO)) {
            Integer userTrainingACProblemCount = trainingProblemEntityService
                    .getUserTrainingACProblemCount(userRolesVO.getUid(), trainingProblemIdList);
            trainingVO.setAcCount(userTrainingACProblemCount);
        } else {
            trainingVO.setAcCount(0);
        }

        return trainingVO;
    }

    /**
     * @param tid
     * @MethodName getTrainingProblemList
     * @Description 根据tid获取指定训练的题单题目列表
     * @Return
     * @Since 2021/11/20
     */
    @Override
    public List<ProblemVO> getTrainingProblemList(Long tid) {

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        trainingValidator.validateTrainingAuth(training);

        return trainingProblemEntityService.getTrainingProblemList(tid);

    }

    /**
     * @param registerTrainingDTO
     * @MethodName toRegisterTraining
     * @Description 注册校验私有权限的训练
     * @Return
     * @Since 2021/11/20
     */
    @Override
    public void toRegisterTraining(RegisterTrainingDTO registerTrainingDTO) {

        Long tid = registerTrainingDTO.getTid();
        String password = registerTrainingDTO.getPassword();

        if (tid == null || StrUtil.isEmpty(password)) {
            throw new StatusFailException("请求参数不能为空！");
        }

        Training training = trainingEntityService.getById(tid);

        if (training == null || !training.getStatus()) {
            throw new StatusFailException("对不起，该训练不存在或不允许显示!");
        }

        if (!training.getPrivatePwd().equals(password)) {
            throw new StatusFailException("训练密码错误，请重新输入！");
        }

        // 获取当前登录的用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        QueryWrapper<TrainingRegister> registerQueryWrapper = new QueryWrapper<>();
        registerQueryWrapper.eq("tid", tid).eq("uid", userRolesVO.getUid());
        if (trainingRegisterEntityService.count(registerQueryWrapper) > 0) {
            throw new StatusFailException("您已注册过该训练，请勿重复注册！");
        }

        boolean isOk = trainingRegisterEntityService
                .save(new TrainingRegister().setTid(tid).setUid(userRolesVO.getUid()));

        if (!isOk) {
            throw new StatusFailException("校验训练密码失败，请稍后再试");
        } else {
            adminTrainingRecordService.syncUserSubmissionToRecordByTid(tid, userRolesVO.getUid());
        }
    }

    /**
     * @param tid
     * @MethodName getTrainingAccess
     * @Description 私有权限的训练需要获取当前用户是否有进入训练的权限
     * @Return
     * @Since 2021/11/20
     */
    @Override
    public AccessVO getTrainingAccess(Long tid) {
        // 获取当前登录的用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        QueryWrapper<TrainingRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid).eq("uid", userRolesVO.getUid());
        TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(queryWrapper, false);
        boolean access = false;
        if (trainingRegister != null) {
            access = true;
            Training training = trainingEntityService.getById(tid);
            if (training == null || !training.getStatus()) {
                throw new StatusFailException("对不起，该训练不存在!");
            }
        }

        AccessVO accessVO = new AccessVO();
        accessVO.setAccess(access);

        return accessVO;
    }

    /**
     * @param tid
     * @param limit
     * @param currentPage
     * @MethodName getTrainingRnk
     * @Description 获取训练的排行榜分页
     * @Return
     * @Since 2021/11/22
     */
    @Override
    public IPage<TrainingRankVO> getTrainingRank(Long tid, Integer limit, Integer currentPage) {

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        trainingValidator.validateTrainingAuth(training);

        // 页数，每页数若为空，设置默认值
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 30;
        }

        return getTrainingRank(tid, training.getAuthor(), currentPage, limit);
    }

    // TODO 行数过多
    private IPage<TrainingRankVO> getTrainingRank(Long tid, String username, int currentPage, int limit) {

        Map<Long, String> tpIdMapDisplayId = getTPIdMapDisplayId(tid);
        List<TrainingRecordVO> trainingRecordVOList = trainingRecordEntityService.getTrainingRecord(tid);

        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();

        List<TrainingRankVO> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();
        int pos = 0;
        for (TrainingRecordVO trainingRecordVO : trainingRecordVOList) {
            // 超级管理员和训练创建者的提交不入排行榜
            if (username.equals(trainingRecordVO.getUsername())
                    || superAdminUidList.contains(trainingRecordVO.getUid())) {
                continue;
            }

            TrainingRankVO trainingRankVO;
            Integer index = uidMapIndex.get(trainingRecordVO.getUid());
            if (index == null) {
                trainingRankVO = new TrainingRankVO();
                trainingRankVO.setRealname(trainingRecordVO.getRealname()).setAvatar(trainingRecordVO.getAvatar())
                        .setSchool(trainingRecordVO.getSchool()).setGender(trainingRecordVO.getGender())
                        .setUid(trainingRecordVO.getUid()).setUsername(trainingRecordVO.getUsername())
                        .setNickname(trainingRecordVO.getNickname()).setAc(0).setTotalRunTime(0);
                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                trainingRankVO.setSubmissionInfo(submissionInfo);

                result.add(trainingRankVO);
                uidMapIndex.put(trainingRecordVO.getUid(), pos);
                pos++;
            } else {
                trainingRankVO = result.get(index);
            }
            String displayId = tpIdMapDisplayId.get(trainingRecordVO.getTpid());
            // TODO
            HashMap<String, Object> problemSubmissionInfo = trainingRankVO.getSubmissionInfo().getOrDefault(displayId,
                    new HashMap<>());

            // 如果该题目已经AC过了，只比较运行时间取最小
            if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                if (trainingRecordVO.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
                    int runTime = (int) problemSubmissionInfo.getOrDefault("runTime", 0);
                    if (runTime > trainingRecordVO.getUseTime()) {
                        trainingRankVO.setTotalRunTime(
                                trainingRankVO.getTotalRunTime() - runTime + trainingRecordVO.getUseTime());
                        problemSubmissionInfo.put("runTime", trainingRecordVO.getUseTime());
                    }
                }
                continue;
            }

            problemSubmissionInfo.put("status", trainingRecordVO.getStatus());
            problemSubmissionInfo.put("score", trainingRecordVO.getScore());

            // 通过的话
            if (trainingRecordVO.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
                // 总解决题目次数ac+1
                trainingRankVO.setAc(trainingRankVO.getAc() + 1);
                problemSubmissionInfo.put("isAC", true);
                problemSubmissionInfo.put("runTime", trainingRecordVO.getUseTime());
                trainingRankVO.setTotalRunTime(trainingRankVO.getTotalRunTime() + trainingRecordVO.getUseTime());
            }

            trainingRankVO.getSubmissionInfo().put(displayId, problemSubmissionInfo);
        }

        List<TrainingRankVO> orderResultList = result.stream()
                // 先以总ac数降序
                .sorted(Comparator.comparing(TrainingRankVO::getAc, Comparator.reverseOrder())
                        .thenComparing(TrainingRankVO::getTotalRunTime))
                .collect(Collectors.toList());

        // 计算好排行榜，然后进行分页
        Page<TrainingRankVO> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<TrainingRankVO> pageList = new ArrayList<>();
        // 计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(orderResultList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);
        return page;
    }

    private Map<Long, String> getTPIdMapDisplayId(Long tid) {
        QueryWrapper<TrainingProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid);
        List<TrainingProblem> trainingProblemList = trainingProblemEntityService.list(queryWrapper);
        return trainingProblemList.stream()
                .collect(Collectors.toMap(TrainingProblem::getId, TrainingProblem::getDisplayId));
    }

    /**
     * 未启用，该操作会导致公开训练也记录，但其实并不需要，会造成数据量无效增加
     */
    @Override
    @Async
    public void checkAndSyncTrainingRecord(Long pid, Long submitId, String uid) {
        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("pid", pid);

        List<TrainingProblem> trainingProblemList = trainingProblemEntityService.list(trainingProblemQueryWrapper);
        List<TrainingRecord> trainingRecordList = new ArrayList<>();
        for (TrainingProblem trainingProblem : trainingProblemList) {
            TrainingRecord trainingRecord = new TrainingRecord();
            trainingRecord.setPid(pid)
                    .setTid(trainingProblem.getTid())
                    .setTpid(trainingProblem.getId())
                    .setSubmitId(submitId).setUid(uid);
            trainingRecordList.add(trainingRecord);
        }
        if (trainingRecordList.size() > 0) {
            trainingRecordEntityService.saveBatch(trainingRecordList);
        }
    }

}