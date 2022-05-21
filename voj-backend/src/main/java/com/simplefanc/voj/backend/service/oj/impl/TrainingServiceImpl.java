package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.training.*;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.pojo.dto.RegisterTrainingDto;
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
    public IPage<TrainingVo> getTrainingList(Integer limit, Integer currentPage, String keyword, Long categoryId,
                                             String auth) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;
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
    public TrainingVo getTraining(Long tid) {
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        boolean isRoot = UserSessionUtil.isRoot();

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        TrainingVo trainingVo = BeanUtil.copyProperties(training, TrainingVo.class);
        TrainingCategory trainingCategory = trainingCategoryEntityService
                .getTrainingCategoryByTrainingId(training.getId());
        trainingVo.setCategoryName(trainingCategory.getName());
        trainingVo.setCategoryColor(trainingCategory.getColor());
        List<Long> trainingProblemIdList = trainingProblemEntityService.getTrainingProblemIdList(training.getId());
        trainingVo.setProblemCount(trainingProblemIdList.size());

        if (userRolesVo != null && trainingValidator.isInTrainingOrAdmin(training, userRolesVo)) {
            Integer userTrainingACProblemCount = trainingProblemEntityService
                    .getUserTrainingACProblemCount(userRolesVo.getUid(), trainingProblemIdList);
            trainingVo.setAcCount(userTrainingACProblemCount);
        } else {
            trainingVo.setAcCount(0);
        }

        return trainingVo;
    }

    /**
     * @param tid
     * @MethodName getTrainingProblemList
     * @Description 根据tid获取指定训练的题单题目列表
     * @Return
     * @Since 2021/11/20
     */
    @Override
    public List<ProblemVo> getTrainingProblemList(Long tid) {

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        trainingValidator.validateTrainingAuth(training);

        return trainingProblemEntityService.getTrainingProblemList(tid);

    }

    /**
     * @param registerTrainingDto
     * @MethodName toRegisterTraining
     * @Description 注册校验私有权限的训练
     * @Return
     * @Since 2021/11/20
     */
    @Override
    public void toRegisterTraining(RegisterTrainingDto registerTrainingDto) {

        Long tid = registerTrainingDto.getTid();
        String password = registerTrainingDto.getPassword();

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
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        QueryWrapper<TrainingRegister> registerQueryWrapper = new QueryWrapper<>();
        registerQueryWrapper.eq("tid", tid).eq("uid", userRolesVo.getUid());
        if (trainingRegisterEntityService.count(registerQueryWrapper) > 0) {
            throw new StatusFailException("您已注册过该训练，请勿重复注册！");
        }

        boolean isOk = trainingRegisterEntityService
                .save(new TrainingRegister().setTid(tid).setUid(userRolesVo.getUid()));

        if (!isOk) {
            throw new StatusFailException("校验训练密码失败，请稍后再试");
        } else {
            adminTrainingRecordService.syncUserSubmissionToRecordByTid(tid, userRolesVo.getUid());
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
    public AccessVo getTrainingAccess(Long tid) {

        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        QueryWrapper<TrainingRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid).eq("uid", userRolesVo.getUid());
        TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(queryWrapper, false);
        boolean access = false;
        if (trainingRegister != null) {
            access = true;
            Training training = trainingEntityService.getById(tid);
            if (training == null || !training.getStatus()) {
                throw new StatusFailException("对不起，该训练不存在!");
            }
        }

        AccessVo accessVo = new AccessVo();
        accessVo.setAccess(access);

        return accessVo;
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
    public IPage<TrainingRankVo> getTrainingRank(Long tid, Integer limit, Integer currentPage) {

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        trainingValidator.validateTrainingAuth(training);

        // 页数，每页数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        return getTrainingRank(tid, training.getAuthor(), currentPage, limit);
    }

    // TODO 行数过多
    private IPage<TrainingRankVo> getTrainingRank(Long tid, String username, int currentPage, int limit) {

        Map<Long, String> tpIdMapDisplayId = getTPIdMapDisplayId(tid);
        List<TrainingRecordVo> trainingRecordVoList = trainingRecordEntityService.getTrainingRecord(tid);

        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();

        List<TrainingRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();
        int pos = 0;
        for (TrainingRecordVo trainingRecordVo : trainingRecordVoList) {
            // 超级管理员和训练创建者的提交不入排行榜
            if (username.equals(trainingRecordVo.getUsername())
                    || superAdminUidList.contains(trainingRecordVo.getUid())) {
                continue;
            }

            TrainingRankVo trainingRankVo;
            Integer index = uidMapIndex.get(trainingRecordVo.getUid());
            if (index == null) {
                trainingRankVo = new TrainingRankVo();
                trainingRankVo.setRealname(trainingRecordVo.getRealname()).setAvatar(trainingRecordVo.getAvatar())
                        .setSchool(trainingRecordVo.getSchool()).setGender(trainingRecordVo.getGender())
                        .setUid(trainingRecordVo.getUid()).setUsername(trainingRecordVo.getUsername())
                        .setNickname(trainingRecordVo.getNickname()).setAc(0).setTotalRunTime(0);
                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                trainingRankVo.setSubmissionInfo(submissionInfo);

                result.add(trainingRankVo);
                uidMapIndex.put(trainingRecordVo.getUid(), pos);
                pos++;
            } else {
                trainingRankVo = result.get(index);
            }
            String displayId = tpIdMapDisplayId.get(trainingRecordVo.getTpid());
            // TODO 键名
            HashMap<String, Object> problemSubmissionInfo = trainingRankVo.getSubmissionInfo().getOrDefault(displayId,
                    new HashMap<>());

            // 如果该题目已经AC过了，只比较运行时间取最小
            if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                if (trainingRecordVo.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
                    int runTime = (int) problemSubmissionInfo.getOrDefault("runTime", 0);
                    if (runTime > trainingRecordVo.getUseTime()) {
                        trainingRankVo.setTotalRunTime(
                                trainingRankVo.getTotalRunTime() - runTime + trainingRecordVo.getUseTime());
                        problemSubmissionInfo.put("runTime", trainingRecordVo.getUseTime());
                    }
                }
                continue;
            }

            problemSubmissionInfo.put("status", trainingRecordVo.getStatus());
            problemSubmissionInfo.put("score", trainingRecordVo.getScore());

            // 通过的话
            if (trainingRecordVo.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
                // 总解决题目次数ac+1
                trainingRankVo.setAc(trainingRankVo.getAc() + 1);
                problemSubmissionInfo.put("isAC", true);
                problemSubmissionInfo.put("runTime", trainingRecordVo.getUseTime());
                trainingRankVo.setTotalRunTime(trainingRankVo.getTotalRunTime() + trainingRecordVo.getUseTime());
            }

            trainingRankVo.getSubmissionInfo().put(displayId, problemSubmissionInfo);
        }

        List<TrainingRankVo> orderResultList = result.stream()
                // 先以总ac数降序
                .sorted(Comparator.comparing(TrainingRankVo::getAc, Comparator.reverseOrder())
                        .thenComparing(TrainingRankVo::getTotalRunTime))
                .collect(Collectors.toList());

        // 计算好排行榜，然后进行分页
        Page<TrainingRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<TrainingRankVo> pageList = new ArrayList<>();
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