package com.simplefanc.voj.backend.service.oj;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.constants.TrainingEnum;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.exception.StatusNotFoundException;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingProblemEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingRecordEntityService;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.backend.validator.TrainingValidator;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.constants.ProblemEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.common.pojo.entity.training.TrainingRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 14:29
 * @Description:
 */
@Component
public class BeforeDispatchInitService {

    @Resource
    private ContestEntityService contestEntityService;

    @Resource
    private ContestRecordEntityService contestRecordEntityService;

    @Resource
    private ContestProblemEntityService contestProblemEntityService;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private TrainingEntityService trainingEntityService;

    @Resource
    private TrainingProblemEntityService trainingProblemEntityService;

    @Resource
    private TrainingRecordEntityService trainingRecordEntityService;

    @Resource
    private TrainingValidator trainingValidator;

    @Resource
    private ContestValidator contestValidator;

    public void initCommonSubmission(String problemId, Judge judge) {

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", problemId);
        Problem problem = problemEntityService.getOne(problemQueryWrapper, false);

        if (problem.getAuth().equals(ProblemEnum.AUTH_PRIVATE.getCode())) {
            throw new StatusForbiddenException("错误！当前题目不可提交！");
        }

        judge.setCpid(0L).setPid(problem.getId()).setDisplayPid(problem.getProblemId());

        // 将新提交数据插入数据库
        judgeEntityService.save(judge);
    }

    @Transactional(rollbackFor = Exception.class)
    public void initContestSubmission(Long cid, String displayId, UserRolesVo userRolesVo, Judge judge) {
        // 首先判断一下比赛的状态是否是正在进行，结束状态都不能提交，比赛前比赛管理员可以提交
        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusNotFoundException("对不起，该比赛不存在！");
        }

        if (contest.getStatus().intValue() == ContestEnum.STATUS_ENDED.getCode()) {
            throw new StatusForbiddenException("比赛已结束，不可再提交！");
        }

        // 是否为超级管理员或者该比赛的创建者，则为比赛管理者
        boolean root = UserSessionUtil.isRoot();
        if (!root && !contest.getUid().equals(userRolesVo.getUid())) {
            if (contest.getStatus().intValue() == ContestEnum.STATUS_SCHEDULED.getCode()) {
                throw new StatusForbiddenException("比赛未开始，不可提交！");
            }
            // 需要检查是否有权限在当前比赛进行提交
            contestValidator.validateJudgeAuth(contest, userRolesVo.getUid());

            // 需要校验当前比赛是否为保护比赛，同时是否开启账号规则限制，如果有，需要对当前用户的用户名进行验证
            if (contest.getAuth().equals(ContestEnum.AUTH_PROTECT.getCode()) && contest.getOpenAccountLimit()
                    && !contestValidator.validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                throw new StatusForbiddenException("对不起！本次比赛只允许特定账号规则的用户参赛！");
            }
        }

        // 查询获取对应的pid和cpid
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).eq("display_id", displayId);
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        judge.setCpid(contestProblem.getId()).setPid(contestProblem.getPid());

        Problem problem = problemEntityService.getById(contestProblem.getPid());
        if (problem.getAuth().equals(ProblemEnum.AUTH_PRIVATE.getCode())) {
            throw new StatusForbiddenException("错误！当前题目不可提交！");
        }
        judge.setDisplayPid(problem.getProblemId());

        // 将新提交数据插入数据库
        judgeEntityService.save(judge);

        // 同时初始化写入contest_record表
        ContestRecord contestRecord = new ContestRecord();
        contestRecord.setDisplayId(displayId).setCpid(contestProblem.getId()).setSubmitId(judge.getSubmitId())
                .setPid(judge.getPid()).setUsername(userRolesVo.getUsername()).setRealname(userRolesVo.getRealname())
                .setUid(userRolesVo.getUid()).setCid(judge.getCid()).setSubmitTime(judge.getSubmitTime());

        if (contest.getStatus().intValue() == ContestEnum.STATUS_SCHEDULED.getCode()) {
            contestRecord.setTime(0L);
        } else {
            // 设置比赛开始时间到提交时间之间的秒数
            contestRecord.setTime(DateUtil.between(contest.getStartTime(), judge.getSubmitTime(), DateUnit.SECOND));
        }
        contestRecordEntityService.save(contestRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    public void initTrainingSubmission(Long tid, String displayId, UserRolesVo userRolesVo, Judge judge) {

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        trainingValidator.validateTrainingAuth(training, userRolesVo);

        // 查询获取对应的pid和cpid
        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid).eq("display_id", displayId);
        TrainingProblem trainingProblem = trainingProblemEntityService.getOne(trainingProblemQueryWrapper);
        judge.setPid(trainingProblem.getPid());

        Problem problem = problemEntityService.getById(trainingProblem.getPid());
        if (problem.getAuth().equals(ProblemEnum.AUTH_PRIVATE.getCode())) {
            throw new StatusForbiddenException("错误！当前题目不可提交！");
        }
        judge.setDisplayPid(problem.getProblemId());

        // 将新提交数据插入数据库
        judgeEntityService.save(judge);

        // 非私有训练不记录
        if (!training.getAuth().equals(TrainingEnum.AUTH_PRIVATE.getValue())) {
            return;
        }

        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setPid(problem.getId()).setTid(tid).setTpid(trainingProblem.getId())
                .setSubmitId(judge.getSubmitId()).setUid(userRolesVo.getUid());
        trainingRecordEntityService.save(trainingRecord);
    }

}