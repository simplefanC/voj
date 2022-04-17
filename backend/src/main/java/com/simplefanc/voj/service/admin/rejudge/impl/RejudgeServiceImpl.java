package com.simplefanc.voj.service.admin.rejudge.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.common.exception.StatusFailException;
import com.simplefanc.voj.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.dao.judge.JudgeCaseEntityService;
import com.simplefanc.voj.dao.judge.JudgeEntityService;
import com.simplefanc.voj.dao.problem.ProblemEntityService;
import com.simplefanc.voj.dao.user.UserAcproblemEntityService;
import com.simplefanc.voj.judge.remote.RemoteJudgeDispatcher;
import com.simplefanc.voj.judge.self.JudgeDispatcher;
import com.simplefanc.voj.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.pojo.entity.judge.Judge;
import com.simplefanc.voj.pojo.entity.judge.JudgeCase;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.service.admin.rejudge.RejudgeService;
import com.simplefanc.voj.utils.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 16:21
 * @Description:
 */
@Service
public class RejudgeServiceImpl implements RejudgeService {

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private UserAcproblemEntityService userAcproblemEntityService;

    @Resource
    private ContestRecordEntityService contestRecordEntityService;

    @Resource
    private JudgeCaseEntityService judgeCaseEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private JudgeDispatcher judgeDispatcher;

    @Resource
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Judge rejudge(Long submitId) {
        Judge judge = judgeEntityService.getById(submitId);

        boolean isContestSubmission = judge.getCid() != 0;
        boolean resetContestRecordResult = true;

        // 如果是非比赛题目
        if (!isContestSubmission) {
            // 重判前，需要将该题目对应记录表一并更新
            // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus().intValue()) {
                QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
                userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
                userAcproblemEntityService.remove(userAcproblemQueryWrapper);
            }
        } else {
            // 将对应比赛记录设置成默认值
            UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("submit_id", submitId).setSql("status=null,score=null");
            resetContestRecordResult = contestRecordEntityService.update(updateWrapper);
        }

        // 清除该提交对应的测试点结果
        QueryWrapper<JudgeCase> judgeCaseQueryWrapper = new QueryWrapper<>();
        judgeCaseQueryWrapper.eq("submit_id", submitId);
        judgeCaseEntityService.remove(judgeCaseQueryWrapper);

        // 设置默认值
        // 开始进入判题队列
        judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus());
        judge.setVersion(judge.getVersion() + 1);
        judge.setJudger("")
                .setTime(null)
                .setMemory(null)
                .setErrorMessage(null)
                .setOiRankScore(null)
                .setScore(null);

        boolean result = judgeEntityService.updateById(judge);


        if (result && resetContestRecordResult) {
            // 调用判题服务
            Problem problem = problemEntityService.getById(judge.getPid());
            // 如果是远程oj判题
            if (problem.getIsRemote()) {
                remoteJudgeDispatcher.sendTask(judge, problem.getProblemId(), isContestSubmission);
            } else {
                judgeDispatcher.sendTask(judge, isContestSubmission);
            }
            return judge;
        } else {
            throw new StatusFailException("重判失败！请重新尝试！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejudgeContestProblem(Long cid, Long pid) {
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", pid);
        List<Judge> rejudgeList = judgeEntityService.list(queryWrapper);

        if (rejudgeList.size() == 0) {
            throw new StatusFailException("当前该题目无提交，不可重判！");
        }

        List<Long> submitIdList = new LinkedList<>();
        HashMap<Long, Integer> idMapStatus = new HashMap<>();
        // 全部设置默认值
        for (Judge judge : rejudgeList) {
            idMapStatus.put(judge.getSubmitId(), judge.getStatus());
            // 开始进入判题队列
            judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus());
            judge.setVersion(judge.getVersion() + 1);
            judge.setJudger("")
                    .setTime(null)
                    .setMemory(null)
                    .setErrorMessage(null)
                    .setOiRankScore(null)
                    .setScore(null);
            submitIdList.add(judge.getSubmitId());
        }
        boolean resetJudgeResult = judgeEntityService.updateBatchById(rejudgeList);
        // 清除每个提交对应的测试点结果
        QueryWrapper<JudgeCase> judgeCaseQueryWrapper = new QueryWrapper<>();
        judgeCaseQueryWrapper.in("submit_id", submitIdList);
        judgeCaseEntityService.remove(judgeCaseQueryWrapper);
        // 将对应比赛记录设置成默认值
        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("submit_id", submitIdList).setSql("status=null,score=null");
        boolean resetContestRecordResult = contestRecordEntityService.update(updateWrapper);

        if (resetContestRecordResult && resetJudgeResult) {
            // 调用重判服务
            Problem problem = problemEntityService.getById(pid);
            // 如果是远程oj判题
            if (problem.getIsRemote()) {
                for (Judge judge : rejudgeList) {
                    // 进入重判队列，等待调用判题服务
                    remoteJudgeDispatcher.sendTask(judge, problem.getProblemId(), judge.getCid() != 0);
                }
            } else {
                for (Judge judge : rejudgeList) {
                    // 进入重判队列，等待调用判题服务
                    judgeDispatcher.sendTask(judge, judge.getCid() != 0);
                }
            }
        } else {
            throw new StatusFailException("重判失败！请重新尝试！");
        }
    }
}