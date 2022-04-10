package com.simplefanc.voj.service.admin.contest.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.dao.contest.ContestEntityService;
import com.simplefanc.voj.pojo.entity.contest.Contest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.simplefanc.voj.common.exception.StatusFailException;
import com.simplefanc.voj.common.exception.StatusForbiddenException;
import com.simplefanc.voj.crawler.problem.ProblemStrategy;
import com.simplefanc.voj.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.dao.judge.JudgeEntityService;
import com.simplefanc.voj.dao.problem.ProblemEntityService;
import com.simplefanc.voj.pojo.dto.ContestProblemDto;
import com.simplefanc.voj.pojo.dto.ProblemDto;
import com.simplefanc.voj.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.pojo.entity.judge.Judge;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.pojo.vo.UserRolesVo;
import com.simplefanc.voj.service.admin.contest.AdminContestProblemService;
import com.simplefanc.voj.service.admin.problem.RemoteProblemService;
import com.simplefanc.voj.utils.Constants;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:20
 * @Description:
 */
@Service
public class AdminContestProblemServiceImpl implements AdminContestProblemService {

    @Autowired
    private ContestProblemEntityService contestProblemEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private RemoteProblemService remoteProblemService;

    @Override
    public HashMap<String, Object> getProblemList(Integer limit, Integer currentPage, String keyword,
                                                  Long cid, Integer problemType, String oj) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Problem> iPage = new Page<>(currentPage, limit);
        // 根据cid在ContestProblem表中查询到对应pid集合
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid);
        List<Long> pidList = new LinkedList<>();

        List<ContestProblem> contestProblemList = contestProblemEntityService.list(contestProblemQueryWrapper);
        HashMap<Long, ContestProblem> contestProblemMap = new HashMap<>();
        contestProblemList.forEach(contestProblem -> {
            contestProblemMap.put(contestProblem.getPid(), contestProblem);
            pidList.add(contestProblem.getPid());
        });

        HashMap<String, Object> contestProblem = new HashMap<>();

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();

        if (problemType != null) { // 必备条件 隐藏的不可取来做比赛题目
            problemQueryWrapper.eq("is_group", false)
                    // vj题目不限制赛制
                    .and(wrapper -> wrapper.eq("type", problemType)
                            .or().eq("is_remote", true))
                    .ne("auth", 2); // 同时需要与比赛相同类型的题目，权限需要是公开的（隐藏的不可加入！）
        }

        // 逻辑判断，如果是查询已有的就应该是in，如果是查询不要重复的，使用not in
        if (problemType != null) {
            problemQueryWrapper.notIn(pidList.size() > 0, "id", pidList);
        } else {
            problemQueryWrapper.in(pidList.size() > 0, "id", pidList);
        }

        // 根据oj筛选过滤
        if (oj != null && !"All".equals(oj)) {
            if (!Constants.RemoteOJ.isRemoteOJ(oj)) {
                problemQueryWrapper.eq("is_remote", false);
            } else {
                problemQueryWrapper.eq("is_remote", true).likeRight("problem_id", oj);
            }
        }

        if (!StringUtils.isEmpty(keyword)) {
            problemQueryWrapper.and(wrapper -> wrapper.like("title", keyword).or()
                    .like("problem_id", keyword).or()
                    .like("author", keyword));
        }

        if (pidList.size() == 0 && problemType == null) {
            problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.eq("id", null);
        }

        IPage<Problem> problemListPage = problemEntityService.page(iPage, problemQueryWrapper);

        if (pidList.size() > 0 && problemType == null) {
            List<Problem> problemList = problemListPage.getRecords();

            List<Problem> sortedProblemList = problemList.stream().sorted(Comparator.comparing(Problem::getId, (a, b) -> {
                ContestProblem x = contestProblemMap.get(a);
                ContestProblem y = contestProblemMap.get(b);
                if (x == null && y != null) {
                    return 1;
                } else if (x != null && y == null) {
                    return -1;
                } else if (x == null) {
                    return -1;
                } else {
                    return x.getDisplayId().compareTo(y.getDisplayId());
                }
            })).collect(Collectors.toList());
            problemListPage.setRecords(sortedProblemList);
        }

        contestProblem.put("problemList", problemListPage);
        contestProblem.put("contestProblemMap", contestProblemMap);

        return contestProblem;
    }

    @Override
    public Problem getProblem(Long pid) {

        Problem problem = problemEntityService.getById(pid);

        if (problem != null) { // 查询成功
            // 获取当前登录的用户
            Session session = SecurityUtils.getSubject().getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

            boolean isRoot = SecurityUtils.getSubject().hasRole("root");
            boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
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
    public void deleteProblem(Long pid, Long cid) {
        //  比赛id不为null，表示就是从比赛列表移除而已
        if (cid != null) {
            QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
            contestProblemQueryWrapper.eq("cid", cid).eq("pid", pid);
            contestProblemEntityService.remove(contestProblemQueryWrapper);
            // 把该题目在比赛的提交全部删掉
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.eq("cid", cid).eq("pid", pid);
            judgeEntityService.remove(judgeUpdateWrapper);
        } else {
             /*
                problem的id为其他表的外键的表中的对应数据都会被一起删除！
              */
            problemEntityService.removeById(pid);
        }

        if (cid == null) {
            FileUtil.del(Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid);
        }
    }

    @Override
    public Map<Object, Object> addProblem(ProblemDto problemDto) {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目的Problem ID已存在，请更换！");
        }
        // 设置为比赛题目
        problemDto.getProblem().setAuth(3);
        boolean isOk = problemEntityService.adminAddProblem(problemDto);
        if (isOk) { // 添加成功
            // 顺便返回新的题目id，好下一步添加外键操作
            return MapUtil.builder().put("pid", problemDto.getProblem().getId()).map();
        } else {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    public void updateProblem(ProblemDto problemDto) {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
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
            contestProblemEntityService.syncContestRecord(contestProblem.getPid(), contestProblem.getCid(), contestProblem.getDisplayId());
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
                .and(wrapper -> wrapper.eq("pid", pid)
                        .or()
                        .eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }

        // 比赛中题目显示默认为原标题
        Problem problem = problemEntityService.getById(pid);
        String displayName = problem.getTitle();

        // 修改成比赛题目
        boolean updateProblem = problemEntityService.saveOrUpdate(problem.setAuth(3));

        boolean isOk = contestProblemEntityService.saveOrUpdate(new ContestProblem()
                .setCid(cid).setPid(pid).setDisplayTitle(displayName).setDisplayId(displayId));
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
            Session session = SecurityUtils.getSubject().getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
            try {
                ProblemStrategy.RemoteProblemInfo otherOJProblemInfo = remoteProblemService.getOtherOJProblemInfo(name.toUpperCase(), problemId, userRolesVo.getUsername());
                if (otherOJProblemInfo != null) {
                    problem = remoteProblemService.adminAddOtherOJProblem(otherOJProblemInfo, name);
                    if (problem == null) {
                        throw new StatusFailException("导入新题目失败！请重新尝试！");
                    }
                } else {
                    throw new StatusFailException("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
                }
            } catch (Exception e) {
                throw new StatusFailException(e.getMessage());
            }
        }

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        Problem finalProblem = problem;
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", finalProblem.getId())
                        .or()
                        .eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }


        // 比赛中题目显示默认为原标题
        String displayName = problem.getTitle();

        // 修改成比赛题目
        boolean updateProblem = problemEntityService.saveOrUpdate(problem.setAuth(3));

        boolean isOk = contestProblemEntityService.saveOrUpdate(new ContestProblem()
                .setCid(cid).setPid(problem.getId()).setDisplayTitle(displayName).setDisplayId(displayId));

        if (!isOk || !updateProblem) {
            throw new StatusFailException("添加失败");
        }
    }

}