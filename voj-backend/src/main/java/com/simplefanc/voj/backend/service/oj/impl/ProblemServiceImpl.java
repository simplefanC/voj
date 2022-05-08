package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.exception.StatusNotFoundException;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.problem.*;
import com.simplefanc.voj.backend.pojo.dto.PidListDto;
import com.simplefanc.voj.backend.pojo.vo.*;
import com.simplefanc.voj.backend.service.oj.ProblemService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.constants.*;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 10:37
 * @Description:
 */
@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Autowired
    private CodeTemplateEntityService codeTemplateEntityService;

    @Autowired
    private ContestValidator contestValidator;

    /**
     * @MethodName getProblemList
     * @Params * @param null
     * @Description 获取题目列表分页
     * @Since 2020/10/27
     */
    @Override
    public Page<ProblemVo> getProblemList(Integer limit, Integer currentPage, String keyword, List<Long> tagId,
                                          Integer difficulty, String oj) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;

        // 关键词查询不为空
        if (!StrUtil.isEmpty(keyword)) {
            keyword = keyword.trim();
        }
        if (oj != null && !RemoteOj.isRemoteOJ(oj)) {
            oj = Constant.LOCAL;
        }
        return problemEntityService.getProblemList(limit, currentPage, null, keyword, difficulty, tagId, oj);
    }

    /**
     * @MethodName getRandomProblem
     * @Description 随机选取一道题目
     * @Since 2020/10/27
     */
    @Override
    public RandomProblemVo getRandomProblem() {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        // 必须是公开题目
        queryWrapper.select("problem_id").eq("auth", 1);
        List<Problem> list = problemEntityService.list(queryWrapper);
        if (list.size() == 0) {
            throw new StatusFailException("获取随机题目失败，题库暂无公开题目！");
        }
        Random random = new Random();
        int index = random.nextInt(list.size());
        RandomProblemVo randomProblemVo = new RandomProblemVo();
        randomProblemVo.setProblemId(list.get(index).getProblemId());
        return randomProblemVo;
    }

    /**
     * @MethodName getUserProblemStatus
     * @Description 获取用户对应该题目列表中各个题目的做题情况
     * @Since 2020/12/29
     */
    // TODO 行数过多
    @Override
    public HashMap<Long, Object> getUserProblemStatus(PidListDto pidListDto) {

        // 需要获取一下该token对应用户的数据
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        HashMap<Long, Object> result = new HashMap<>();
        // 先查询判断该用户对于这些题是否已经通过，若已通过，则无论后续再提交结果如何，该题都标记为通过
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("distinct pid,status,submit_time,score").in("pid", pidListDto.getPidList())
                .eq("uid", userRolesVo.getUid()).orderByDesc("submit_time");

        if (pidListDto.getIsContestProblemList()) {
            // 如果是比赛的提交记录需要判断cid
            queryWrapper.eq("cid", pidListDto.getCid());
        } else {
            queryWrapper.eq("cid", 0);
        }

        List<Judge> judges = judgeEntityService.list(queryWrapper);

        boolean isACMContest = true;
        Contest contest = null;
        if (pidListDto.getIsContestProblemList()) {
            contest = contestEntityService.getById(pidListDto.getCid());
            if (contest == null) {
                throw new StatusNotFoundException("错误：该比赛不存在！");
            }
            isACMContest = contest.getType().intValue() == ContestEnum.TYPE_ACM.getCode();
        }

        for (Judge judge : judges) {
            // 如果是比赛的题目列表状态
            HashMap<String, Object> temp = new HashMap<>();
            if (pidListDto.getIsContestProblemList()) {
                if (!isACMContest) {
                    // IO比赛的，如果还未写入，则使用最新一次提交的结果
                    if (!result.containsKey(judge.getPid())) {
                        // 判断该提交是否为封榜之后的提交,OI赛制封榜后的提交看不到提交结果，
                        // 只有比赛结束可以看到,比赛管理员与超级管理员的提交除外
                        if (contestValidator.isSealRank(userRolesVo.getUid(), contest, true,
                                UserSessionUtil.isRoot())) {
                            temp.put("status", JudgeStatus.STATUS_SUBMITTED_UNKNOWN_RESULT.getStatus());
                            temp.put("score", null);
                        } else {
                            temp.put("status", judge.getStatus());
                            temp.put("score", judge.getScore());
                        }
                        result.put(judge.getPid(), temp);
                    }
                } else {
                    // 如果该题目已通过，且同时是为不封榜前提交的，则强制写为通过（0）
                    if (judge.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
                        temp.put("status", JudgeStatus.STATUS_ACCEPTED.getStatus());
                        temp.put("score", null);
                        result.put(judge.getPid(), temp);
                    } else if (!result.containsKey(judge.getPid())) {
                        // 还未写入，则使用最新一次提交的结果
                        temp.put("status", judge.getStatus());
                        temp.put("score", null);
                        result.put(judge.getPid(), temp);
                    }
                }

            } else { // 不是比赛题目
                // 如果该题目已通过，则强制写为通过（0）
                if (judge.getStatus().intValue() == JudgeStatus.STATUS_ACCEPTED.getStatus()) {
                    temp.put("status", JudgeStatus.STATUS_ACCEPTED.getStatus());
                    result.put(judge.getPid(), temp);
                } else if (!result.containsKey(judge.getPid())) {
                    // 还未写入，则使用最新一次提交的结果
                    temp.put("status", judge.getStatus());
                    result.put(judge.getPid(), temp);
                }
            }
        }

        // 再次检查，应该可能从未提交过该题，则状态写为-10
        for (Long pid : pidListDto.getPidList()) {

            // 如果是比赛的题目列表状态
            if (pidListDto.getIsContestProblemList()) {
                if (!result.containsKey(pid)) {
                    HashMap<String, Object> temp = new HashMap<>();
                    temp.put("score", null);
                    temp.put("status", JudgeStatus.STATUS_NOT_SUBMITTED.getStatus());
                    result.put(pid, temp);
                }
            } else {
                if (!result.containsKey(pid)) {
                    HashMap<String, Object> temp = new HashMap<>();
                    temp.put("status", JudgeStatus.STATUS_NOT_SUBMITTED.getStatus());
                    result.put(pid, temp);
                }
            }
        }
        return result;

    }

    /**
     * @MethodName getProblemInfo
     * @Description 获取指定题目的详情信息，标签，所支持语言，做题情况（只能查询公开题目 也就是auth为1）
     * @Since 2020/10/27
     */
    @Override
    public ProblemInfoVo getProblemInfo(String problemId) {

        QueryWrapper<Problem> wrapper = new QueryWrapper<Problem>().eq("problem_id", problemId);
        // 查询题目详情，题目标签，题目语言，题目做题情况
        Problem problem = problemEntityService.getOne(wrapper, false);
        if (problem == null) {
            throw new StatusNotFoundException("该题号对应的题目不存在");
        }
        if (!problem.getAuth().equals(ProblemEnum.AUTH_PUBLIC.getCode())) {
            throw new StatusForbiddenException("该题号对应题目并非公开题目，不支持访问！");
        }

        QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
        problemTagQueryWrapper.eq("pid", problem.getId());
        // 获取该题号对应的标签id
        List<Long> tidList = new LinkedList<>();
        problemTagEntityService.list(problemTagQueryWrapper).forEach(problemTag -> {
            tidList.add(problemTag.getTid());
        });

        List<Tag> tags = (List<Tag>) tagEntityService.listByIds(tidList);

        // 记录 languageId对应的name
        HashMap<Long, String> tmpMap = new HashMap<>();

        // 获取题目提交的代码支持的语言
        List<String> languagesStr = new LinkedList<>();
        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", problem.getId()).select("lid");
        List<Long> lidList = problemLanguageEntityService.list(problemLanguageQueryWrapper).stream()
                .map(ProblemLanguage::getLid).collect(Collectors.toList());
        languageEntityService.listByIds(lidList).forEach(language -> {
            languagesStr.add(language.getName());
            tmpMap.put(language.getId(), language.getName());
        });

        // 获取题目的提交记录
        ProblemCountVo problemCount = judgeEntityService.getProblemCount(problem.getId());

        // 获取题目的代码模板
        QueryWrapper<CodeTemplate> codeTemplateQueryWrapper = new QueryWrapper<>();
        codeTemplateQueryWrapper.eq("pid", problem.getId()).eq("status", true);
        List<CodeTemplate> codeTemplates = codeTemplateEntityService.list(codeTemplateQueryWrapper);
        HashMap<String, String> LangNameAndCode = new HashMap<>();
        if (codeTemplates.size() > 0) {
            for (CodeTemplate codeTemplate : codeTemplates) {
                LangNameAndCode.put(tmpMap.get(codeTemplate.getLid()), codeTemplate.getCode());
            }
        }
        // 屏蔽一些题目参数
        problem.setJudgeExtraFile(null).setSpjCode(null).setSpjLanguage(null);

        // 将数据统一写入到一个Vo返回数据实体类中
        ProblemInfoVo problemInfoVo = new ProblemInfoVo(problem, tags, languagesStr, problemCount, LangNameAndCode);
        return problemInfoVo;
    }

}