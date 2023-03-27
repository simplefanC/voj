package com.simplefanc.voj.backend.service.oj.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.exception.StatusAccessDeniedException;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.exception.StatusNotFoundException;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeCaseEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.dao.user.UserAcproblemEntityService;
import com.simplefanc.voj.backend.judge.local.JudgeTaskDispatcher;
import com.simplefanc.voj.backend.judge.remote.RemoteJudgeTaskDispatcher;
import com.simplefanc.voj.backend.pojo.dto.SubmitIdListDTO;
import com.simplefanc.voj.backend.pojo.dto.ToJudgeDTO;
import com.simplefanc.voj.backend.config.ConfigVO;
import com.simplefanc.voj.backend.pojo.vo.JudgeVO;
import com.simplefanc.voj.backend.pojo.vo.SubmissionInfoVO;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.service.oj.BeforeDispatchInitService;
import com.simplefanc.voj.backend.service.oj.JudgeService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.backend.validator.JudgeValidator;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.constants.RedisConstant;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeCase;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.common.utils.IpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 11:12
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final JudgeEntityService judgeEntityService;

    private final JudgeCaseEntityService judgeCaseEntityService;

    private final ProblemEntityService problemEntityService;

    private final ContestEntityService contestEntityService;

    private final ContestRecordEntityService contestRecordEntityService;

    private final UserAcproblemEntityService userAcproblemEntityService;

    private final JudgeTaskDispatcher judgeTaskDispatcher;

    private final RemoteJudgeTaskDispatcher remoteJudgeTaskDispatcher;

    private final RedisUtil redisUtil;

    private final JudgeValidator judgeValidator;

    private final ContestValidator contestValidator;

    private final BeforeDispatchInitService beforeDispatchInitService;

    private final ConfigVO configVO;

    /**
     * @MethodName submitProblemJudge
     * @Description 核心方法
     * @Since 2021/10/30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Judge submitProblemJudge(ToJudgeDTO judgeDTO) {

        judgeValidator.validateSubmissionInfo(judgeDTO);

        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        boolean isContestSubmission = judgeDTO.getCid() != 0;

        boolean isTrainingSubmission = judgeDTO.getTid() != null && judgeDTO.getTid() != 0;

        // 非比赛提交有限制
        if (!isContestSubmission && configVO.getDefaultSubmitInterval() > 0) {
            String lockKey = RedisConstant.SUBMIT_NON_CONTEST_LOCK + userRolesVO.getUid();
            long count = redisUtil.incr(lockKey, 1);
            if (count > 1) {
                throw new StatusForbiddenException("对不起，您的提交频率过快，请稍后再尝试！");
            }
            redisUtil.expire(lockKey, configVO.getDefaultSubmitInterval());
        }

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes()))
                .getRequest();
        // 将提交先写入数据库，准备调用判题服务器
        Judge judge = new Judge();
        // 默认设置代码为单独自己可见
        judge.setShare(false).setCode(judgeDTO.getCode()).setCid(judgeDTO.getCid()).setLanguage(judgeDTO.getLanguage())
                .setLength(judgeDTO.getCode().length()).setUid(userRolesVO.getUid())
                .setUsername(userRolesVO.getUsername())
                // 开始进入判题队列
                .setStatus(JudgeStatus.STATUS_PENDING.getStatus()).setSubmitTime(new Date()).setVersion(0)
                .setIp(IpUtil.getUserIpAddr(request));

        // 如果比赛id不等于0，则说明为比赛提交
        if (isContestSubmission) {
            beforeDispatchInitService.initContestSubmission(judgeDTO.getCid(), judgeDTO.getPid(), judge);
        } else if (isTrainingSubmission) {
            beforeDispatchInitService.initTrainingSubmission(judgeDTO.getTid(), judgeDTO.getPid(), judge);
        } else { // 如果不是比赛提交和训练提交
            beforeDispatchInitService.initCommonSubmission(judgeDTO.getPid(), judge);
        }

        // 将提交加入任务队列
        if (judgeDTO.getIsRemote()) {
            // 如果是远程oj判题
            final String remoteJudgeProblem = judge.getDisplayPid();
            remoteJudgeTaskDispatcher.sendTask(judge, remoteJudgeProblem, isContestSubmission);
        } else {
            judgeTaskDispatcher.sendTask(judge, isContestSubmission);
        }

        return judge;
    }

    /**
     * @MethodName resubmit
     * @Description 调用判题服务器提交失败超过60s后，用户点击按钮重新提交判题进入的方法
     * @Since 2021/2/12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Judge resubmit(Long submitId) {

        Judge judge = judgeEntityService.getById(submitId);
        if (judge == null) {
            throw new StatusNotFoundException("此提交数据不存在！");
        }

        Problem problem = problemEntityService.getById(judge.getPid());

        // 如果是非比赛题目
        if (judge.getCid() == 0) {
            // 重判前，需要将该题目对应记录表一并更新
            // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
            if (judge.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus().intValue()) {
                QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
                userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
                userAcproblemEntityService.remove(userAcproblemQueryWrapper);
            }
        } else {
            if (problem.getIsRemote()) {
                // 将对应比赛记录设置成默认值
                UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("submit_id", submitId).setSql("status=null,score=null");
                contestRecordEntityService.update(updateWrapper);
            } else {
                throw new StatusNotFoundException("错误！非vJudge题目在比赛过程无权限重新提交");
            }
        }

        // 重新进入等待队列
        judge.setStatus(JudgeStatus.STATUS_PENDING.getStatus());
        judge.setVersion(judge.getVersion() + 1);
        judge.setErrorMessage(null).setOiRankScore(null).setScore(null).setTime(null).setJudger("").setMemory(null);
        judgeEntityService.updateById(judge);

        // 将提交加入任务队列
        if (problem.getIsRemote()) {
            // 如果是远程oj判题
            remoteJudgeTaskDispatcher.sendTask(judge, problem.getProblemId(), judge.getCid() != 0);
        } else {
            judgeTaskDispatcher.sendTask(judge, judge.getCid() != 0);
        }
        return judge;
    }

    /**
     * @MethodName getSubmission
     * @Description 获取单个提交记录的详情
     * @Since 2021/1/2
     */
    @Override
    public SubmissionInfoVO getSubmission(Long submitId) {
        Judge judge = judgeEntityService.getById(submitId);
        if (judge == null) {
            throw new StatusNotFoundException("此提交数据不存在！");
        }

        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();
        if (userRolesVO == null) {
            throw new StatusAccessDeniedException("请先登录！");
        }

        // 是否为超级管理员
        boolean isRoot = UserSessionUtil.isRoot();
        // 是否为题目管理员
        boolean problemAdmin = UserSessionUtil.isProblemAdmin();
        // 限制：后台配置的时间 之前的代码 都不能查看
        if (!isRoot && !problemAdmin && judge.getSubmitTime().getTime() < configVO.getCodeVisibleStartTime()) {
            throw new StatusNotFoundException("此提交数据当前时间无法查看！");
        }
        // 清空vj信息
        judge.setVjudgeUsername(null);
        judge.setVjudgeSubmitId(null);
        judge.setVjudgePassword(null);

        // 超级管理员与题目管理员有权限查看代码
        // 如果不是本人或者并未分享代码，则不可查看
        // 当此次提交代码不共享
        // 比赛提交只有比赛创建者和root账号可看代码
        if (judge.getCid() != 0) {
            Contest contest = contestEntityService.getById(judge.getCid());
            if (!contestValidator.isContestAdmin(contest)) {
                // 不是本人的话不能查看代码
                if (!userRolesVO.getUid().equals(judge.getUid())) {
                    judge.setCode(null);
                    // 如果还在比赛时间，不是本人不能查看时间，空间，长度，错误提示信息
                    if (contest.getStatus().intValue() == ContestEnum.STATUS_RUNNING.getCode()) {
                        judge.setTime(null);
                        judge.setMemory(null);
                        judge.setLength(null);
                        judge.setErrorMessage("The contest is in progress. You are not allowed to view other people's error information.");
                    }
                }
            }
        } else {
            if (!judge.getShare() && !isRoot && !problemAdmin) {
                // 需要判断是否为当前登陆用户自己的提交代码
                if (!judge.getUid().equals(userRolesVO.getUid())) {
                    judge.setCode(null);
                }
            }
        }

        // 只允许用户查看ce错误，sf错误，se错误信息提示
        if (judge.getStatus().intValue() != JudgeStatus.STATUS_COMPILE_ERROR.getStatus()
                && judge.getStatus().intValue() != JudgeStatus.STATUS_SYSTEM_ERROR.getStatus()
                && judge.getStatus().intValue() != JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus()) {
            judge.setErrorMessage("The error message does not support viewing.");
        }

        Problem problem = problemEntityService.getById(judge.getPid());
        return new SubmissionInfoVO()
                .setSubmission(judge)
                .setCodeShare(problem.getCodeShare());
    }

    /**
     * @MethodName updateSubmission
     * @Description 修改单个提交详情的分享权限
     * @Since 2021/1/2
     */
    @Override
    public void updateSubmission(Judge judge) {
        // 需要获取一下该token对应用户的数据
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        // 判断该提交是否为当前用户的
        if (!userRolesVO.getUid().equals(judge.getUid())) {
            throw new StatusForbiddenException("对不起，您不能修改他人的代码分享权限！");
        }
        Judge judgeInfo = judgeEntityService.getById(judge.getSubmitId());
        if (judgeInfo.getCid() != 0) {
            // 如果是比赛提交，不可分享！
            throw new StatusForbiddenException("对不起，您不能分享比赛题目的提交代码！");
        }
        judgeInfo.setShare(judge.getShare());
        boolean isOk = judgeEntityService.updateById(judgeInfo);
        if (!isOk) {
            throw new StatusFailException("修改代码权限失败！");
        }
    }

    /**
     * @MethodName getJudgeList
     * @Description 通用查询判题记录列表
     * @Since 2021/10/29
     */
    @Override
    public IPage<JudgeVO> getJudgeList(Integer limit, Integer currentPage, Boolean onlyMine, String searchPid,
                                       Integer searchStatus, String searchUsername, Boolean completeProblemId) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 30;
        }

        String uid = null;
        // 只查看当前用户的提交
        if (onlyMine) {
            UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

            if (userRolesVO == null) {
                throw new StatusAccessDeniedException("当前用户数据为空，请您重新登陆！");
            }
            uid = userRolesVO.getUid();
        }
        if (searchPid != null) {
            searchPid = searchPid.trim();
        }
        if (searchUsername != null) {
            searchUsername = searchUsername.trim();
        }

        return judgeEntityService.getCommonJudgeList(limit, currentPage, searchPid, searchStatus, searchUsername, uid,
                completeProblemId);
    }

    /**
     * @MethodName checkJudgeResult
     * @Description 对提交列表状态为Pending和Judging的提交进行更新检查
     * @Since 2021/1/3
     */
    @Override
    public HashMap<Long, Object> checkCommonJudgeResult(SubmitIdListDTO submitIdListDTO) {

        List<Long> submitIds = submitIdListDTO.getSubmitIds();

        if (CollectionUtils.isEmpty(submitIds)) {
            return new HashMap<>();
        }

        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        // lambada表达式过滤掉code
        queryWrapper.select(Judge.class, info -> !"code".equals(info.getColumn())).in("submit_id", submitIds);
        List<Judge> judgeList = judgeEntityService.list(queryWrapper);
        HashMap<Long, Object> result = new HashMap<>();
        for (Judge judge : judgeList) {
            judge.setCode(null);
            judge.setErrorMessage(null);
            judge.setVjudgeUsername(null);
            judge.setVjudgeSubmitId(null);
            judge.setVjudgePassword(null);
            result.put(judge.getSubmitId(), judge);
        }
        return result;
    }

    /**
     * @MethodName checkContestJudgeResult
     * @Description 需要检查是否为封榜，是否可以查询结果，避免有人恶意查询
     * @Since 2021/6/11
     */
    @Override
    public HashMap<Long, Object> checkContestJudgeResult(SubmitIdListDTO submitIdListDTO) {

        if (submitIdListDTO.getCid() == null) {
            throw new StatusNotFoundException("查询比赛id不能为空");
        }

        if (CollectionUtils.isEmpty(submitIdListDTO.getSubmitIds())) {
            return new HashMap<>();
        }

        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        Contest contest = contestEntityService.getById(submitIdListDTO.getCid());

        boolean isSealRank = contestValidator.isOpenSealRank(contest, true);

        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        // lambada表达式过滤掉code
        queryWrapper.select(Judge.class, info -> !"code".equals(info.getColumn()))
                .in("submit_id", submitIdListDTO.getSubmitIds()).eq("cid", submitIdListDTO.getCid())
                .between(isSealRank, "submit_time", contest.getStartTime(), contest.getSealRankTime());
        List<Judge> judgeList = judgeEntityService.list(queryWrapper);
        HashMap<Long, Object> result = new HashMap<>();
        for (Judge judge : judgeList) {
            judge.setCode(null);
            judge.setDisplayPid(null);
            judge.setErrorMessage(null);
            judge.setVjudgeUsername(null);
            judge.setVjudgeSubmitId(null);
            judge.setVjudgePassword(null);
            if (!judge.getUid().equals(userRolesVO.getUid()) && !contestValidator.isContestAdmin(contest)) {
                judge.setTime(null);
                judge.setMemory(null);
                judge.setLength(null);
            }
            result.put(judge.getSubmitId(), judge);
        }
        return result;
    }

    /**
     * @MethodName getJudgeCase
     * @Description 获得指定提交id的测试样例结果，暂不支持查看测试数据，只可看测试点结果，时间，空间，或者IO得分
     * @Since 2021/10/29
     */
    @Override
    public List<JudgeCase> getAllCaseResult(Long submitId) {

        Judge judge = judgeEntityService.getById(submitId);

        if (judge == null) {
            throw new StatusNotFoundException("此提交数据不存在！");
        }

        Problem problem = problemEntityService.getById(judge.getPid());

        // 如果该题不支持开放测试点结果查看
        if (!problem.getOpenCaseResult()) {
            return null;
        }

        // 是否为超级管理员
        boolean isRoot = UserSessionUtil.isRoot();

        if (judge.getCid() != 0) {
            Contest contest = contestEntityService.getById(judge.getCid());
            if (!contestValidator.isContestAdmin(contest)) {
                // 当前是比赛期间 比赛封榜不能看
                if (contest.getSealRank() && contest.getStatus().intValue() == ContestEnum.STATUS_RUNNING.getCode()
                        && contest.getSealRankTime().before(new Date())) {
                    throw new StatusForbiddenException("对不起，该题测试样例详情不能查看！");
                }

                // 若是比赛题目，只支持OI查看测试点情况，ACM强制禁止查看,比赛管理员除外
                if (problem.getType().intValue() == ContestEnum.TYPE_ACM.getCode()) {
                    throw new StatusForbiddenException("对不起，该题测试样例详情不能查看！");
                }
            }
        }

        QueryWrapper<JudgeCase> wrapper = new QueryWrapper<>();

        if (!isRoot && !UserSessionUtil.isAdmin() && !UserSessionUtil.isProblemAdmin()) {
            wrapper.select("time", "memory", "score", "status", "user_output");
        }
        wrapper.eq("submit_id", submitId).last("order by length(input_data) asc,input_data asc");

        // 当前所有测试点只支持 空间 时间 状态码 IO得分 和错误信息提示查看而已
        return judgeCaseEntityService.list(wrapper);
    }


}