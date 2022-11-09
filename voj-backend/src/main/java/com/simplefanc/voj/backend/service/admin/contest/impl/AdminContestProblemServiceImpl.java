package com.simplefanc.voj.backend.service.admin.contest.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.judge.remote.crawler.ProblemCrawler;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.pojo.dto.ContestProblemDto;
import com.simplefanc.voj.backend.pojo.dto.ProblemDto;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.admin.contest.AdminContestProblemService;
import com.simplefanc.voj.backend.service.admin.problem.RemoteProblemService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.constants.ProblemEnum;
import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:20
 * @Description:
 */
@Service
@RequiredArgsConstructor
@Slf4j(topic = "voj")
public class AdminContestProblemServiceImpl implements AdminContestProblemService {

    private final ContestProblemEntityService contestProblemEntityService;

    private final ProblemEntityService problemEntityService;

    private final JudgeEntityService judgeEntityService;

    private final RemoteProblemService remoteProblemService;

    private final FilePathProperties filePathProps;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getProblemList(Integer limit, Integer currentPage, String keyword, Long cid,
                                                  Integer problemType, String oj) {
        // 根据cid在ContestProblem表中查询到对应pid集合
        HashMap<Long, ContestProblem> contestProblemMap = new HashMap<>();
        List<Long> pidList = new LinkedList<>();
        contestProblemEntityService.lambdaQuery()
                .eq(ContestProblem::getCid, cid)
                .list()
                .forEach(contestProblem -> {
                    contestProblemMap.put(contestProblem.getPid(), contestProblem);
                    pidList.add(contestProblem.getPid());
                });

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();

        if (problemType != null) {
            // 从公共题库添加题目
            problemQueryWrapper.notIn(pidList.size() > 0, "id", pidList);

            problemQueryWrapper
                    // 同时需要与比赛相同类型的题目
                    // vj题目不限制赛制
                    .and(wrapper -> wrapper.eq("type", problemType).or().eq("is_remote", true))
                    // 题目权限为隐藏的不可加入！
                    .ne("auth", ProblemEnum.AUTH_PRIVATE.getCode());
        } else {
            // 比赛题目列表
            problemQueryWrapper.in(pidList.size() > 0, "id", pidList);
        }

        // 根据oj筛选过滤
        if (oj != null && !"All".equals(oj)) {
            if (!RemoteOj.isRemoteOJ(oj)) {
                problemQueryWrapper.eq("is_remote", false);
            } else {
                problemQueryWrapper.eq("is_remote", true).likeRight("problem_id", oj);
            }
        }

        // 根据keyword筛选过滤
        if (!StrUtil.isEmpty(keyword)) {
            problemQueryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("problem_id", keyword).or()
                    .like("author", keyword));
        }

        // 比赛题目列表为空
        if (pidList.size() == 0 && problemType == null) {
            problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.eq("id", null);
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;
        if (pidList.size() > 0 && problemType == null) {
            limit = Integer.MAX_VALUE;
        }

        IPage<Problem> problemListPage = problemEntityService.page(new Page<>(currentPage, limit), problemQueryWrapper);

        if (pidList.size() > 0 && problemType == null) {
            List<Problem> sortedProblemList = problemListPage.getRecords().stream()
                    .sorted(Comparator.comparing(Problem::getId, (a, b) -> {
                        ContestProblem x = contestProblemMap.get(a);
                        ContestProblem y = contestProblemMap.get(b);
                        return x.compareTo(y);
                    })).collect(Collectors.toList());
            problemListPage.setRecords(sortedProblemList);
        }

        return MapUtil.builder(new HashMap<String, Object>()).put("problemList", problemListPage)
                .put("contestProblemMap", contestProblemMap)
                .build();
    }

    @Override
    public Problem getProblem(Long pid) {
        Problem problem = problemEntityService.getById(pid);

        if (problem != null) {
            // 获取当前登录的用户
            UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

            boolean isRoot = UserSessionUtil.isRoot();
            boolean isProblemAdmin = UserSessionUtil.isProblemAdmin();
            // 只有超级管理员和题目管理员、题目创建者才能操作
            if (!isRoot && !isProblemAdmin && !userRolesVo.getUsername().equals(problem.getAuthor())) {
                throw new StatusForbiddenException("对不起，你无权限查看题目！");
            }

            return problem;
        } else {
            throw new StatusFailException("查询失败！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProblem(Long pid, Long cid) {
        // 比赛id不为null，表示就是从比赛列表移除而已
        if (cid != null) {
            QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
            contestProblemQueryWrapper.eq("cid", cid).eq("pid", pid);
            contestProblemEntityService.remove(contestProblemQueryWrapper);
            // 把该题目在比赛的提交全部删掉
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.eq("cid", cid).eq("pid", pid);
            judgeEntityService.remove(judgeUpdateWrapper);
        } else {
            // problem的id为其他表的外键的表中的对应数据都会被一起删除！
            problemEntityService.removeById(pid);
        }

        if (cid == null) {
            FileUtil.del(filePathProps.getTestcaseBaseFolder() + File.separator + "problem_" + pid);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Object, Object> addProblem(ProblemDto problemDto) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目的Problem ID已存在，请更换！");
        }
        // 设置为比赛题目
        problemDto.getProblem().setAuth(ProblemEnum.AUTH_CONTEST.getCode());
        boolean isOk = problemEntityService.adminAddProblem(problemDto);
        // 添加成功
        if (isOk) {
            // 顺便返回新的题目id，好下一步添加外键操作
            // TODO put 键
            return MapUtil.builder().put("pid", problemDto.getProblem().getId()).map();
        } else {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProblem(ProblemDto problemDto) {
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        boolean isRoot = UserSessionUtil.isRoot();
        boolean isProblemAdmin = UserSessionUtil.isProblemAdmin();
        // 只有超级管理员和题目管理员、题目创建者才能操作
        if (!isRoot && !isProblemAdmin && !userRolesVo.getUsername().equals(problemDto.getProblem().getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限修改题目！");
        }

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemEntityService.getOne(queryWrapper);

        // 如果problem_id不是原来的且已存在该problem_id，则修改失败！
        if (problem != null && problem.getId().longValue() != problemDto.getProblem().getId()) {
            throw new StatusFailException("当前的Problem ID 已被使用，请重新更换新的！");
        }

        // 记录修改题目的用户
        problemDto.getProblem().setModifiedUser(userRolesVo.getUsername());

        boolean isOk = problemEntityService.adminUpdateProblem(problemDto);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public ContestProblem getContestProblem(Long cid, Long pid) {
        QueryWrapper<ContestProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", pid);
        ContestProblem contestProblem = contestProblemEntityService.getOne(queryWrapper);
        if (contestProblem == null) {
            throw new StatusFailException("查询失败");
        }
        return contestProblem;
    }

    @Override
    public ContestProblem setContestProblem(ContestProblem contestProblem) {
        boolean isOk = contestProblemEntityService.saveOrUpdate(contestProblem);
        if (isOk) {
            contestProblemEntityService.syncContestRecord(contestProblem.getPid(), contestProblem.getCid(),
                    contestProblem.getDisplayId());
            return contestProblem;
        } else {
            throw new StatusFailException("更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProblemFromPublic(ContestProblemDto contestProblemDto) {

        Long pid = contestProblemDto.getPid();
        Long cid = contestProblemDto.getCid();
        String displayId = contestProblemDto.getDisplayId();

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", pid).or().eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }

        // 比赛中题目显示默认为原标题
        Problem problem = problemEntityService.getById(pid);
        String displayName = problem.getTitle();

        // 修改成比赛题目
        boolean updateProblem = problemEntityService.saveOrUpdate(problem.setAuth(ProblemEnum.AUTH_CONTEST.getCode()));

        boolean isOk = contestProblemEntityService.saveOrUpdate(
                new ContestProblem().setCid(cid).setPid(pid).setDisplayTitle(displayName).setDisplayId(displayId));
        if (!isOk || !updateProblem) {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importContestRemoteOJProblem(String name, String problemId, Long cid, String displayId) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        Problem problem = problemEntityService.getOne(queryWrapper, false);

        // 如果该题目不存在，需要先导入
        if (problem == null) {
            try {
                ProblemCrawler.RemoteProblemInfo otherOJProblemInfo = remoteProblemService
                        .getOtherOJProblemInfo(name.toUpperCase(), problemId);
                if (otherOJProblemInfo != null) {
                    problem = remoteProblemService.adminAddOtherOJProblem(otherOJProblemInfo, name);
                    if (problem == null) {
                        throw new StatusFailException("导入新题目失败！请重新尝试！");
                    }
                } else {
                    throw new StatusFailException("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
                }
            } catch (Exception e) {
                log.error("导入远程题目异常-------------->", e);
                throw new StatusFailException(e.getMessage());
            }
        }

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        Problem finalProblem = problem;
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", finalProblem.getId()).or().eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }

        // 比赛中题目显示默认为原标题
        String displayName = problem.getTitle();

        // 修改成比赛题目
        boolean updateProblem = problemEntityService.saveOrUpdate(problem.setAuth(ProblemEnum.AUTH_CONTEST.getCode()));

        boolean isOk = contestProblemEntityService.saveOrUpdate(new ContestProblem().setCid(cid).setPid(problem.getId())
                .setDisplayTitle(displayName).setDisplayId(displayId));

        if (!isOk || !updateProblem) {
            throw new StatusFailException("添加失败");
        }
    }

}