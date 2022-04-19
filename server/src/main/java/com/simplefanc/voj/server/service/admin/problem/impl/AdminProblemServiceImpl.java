package com.simplefanc.voj.server.service.admin.problem.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.common.constants.RemoteOj;
import com.simplefanc.voj.common.pojo.dto.CompileDTO;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.server.common.exception.StatusFailException;
import com.simplefanc.voj.server.common.exception.StatusForbiddenException;
import com.simplefanc.voj.server.dao.judge.JudgeEntityService;
import com.simplefanc.voj.server.dao.problem.ProblemCaseEntityService;
import com.simplefanc.voj.server.dao.problem.ProblemEntityService;
import com.simplefanc.voj.server.judge.Dispatcher;
import com.simplefanc.voj.server.judge.remote.crawler.ProblemCrawler;
import com.simplefanc.voj.server.pojo.bo.FilePathProps;
import com.simplefanc.voj.server.pojo.dto.ProblemDto;
import com.simplefanc.voj.server.pojo.vo.UserRolesVo;
import com.simplefanc.voj.server.service.admin.problem.AdminProblemService;
import com.simplefanc.voj.server.service.admin.problem.RemoteProblemService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 16:32
 * @Description:
 */

@Service
@RefreshScope
public class AdminProblemServiceImpl implements AdminProblemService {
    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemCaseEntityService problemCaseEntityService;

    @Autowired
    private Dispatcher dispatcher;

    @Value("${voj.judge.token}")
    private String judgeToken;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Autowired
    private RemoteProblemService remoteProblemService;

    @Autowired
    private FilePathProps filePathProps;

    @Override
    public IPage<Problem> getProblemList(Integer limit, Integer currentPage, String keyword, Integer auth, String oj) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Problem> iPage = new Page<>(currentPage, limit);
        IPage<Problem> problemList;

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("gmt_create")
                .orderByDesc("id");

        // 根据oj筛选过滤
        if (oj != null && !"All".equals(oj)) {
            if (!RemoteOj.isRemoteOJ(oj)) {
                queryWrapper.eq("is_remote", false);
            } else {
                queryWrapper.eq("is_remote", true).likeRight("problem_id", oj);
            }
        }

        if (auth != null && auth != 0) {
            queryWrapper.eq("auth", auth);
        }

        if (!StringUtils.isEmpty(keyword)) {
            final String key = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like("title", key).or()
                    .like("author", key).or()
                    .like("problem_id", key));
        }
        problemList = problemEntityService.page(iPage, queryWrapper);
        return problemList;
    }

    @Override
    public Problem getProblem(Long pid) {
        Problem problem = problemEntityService.getById(pid);

        // 查询成功
        if (problem != null) {
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
    public void deleteProblem(Long pid) {
        boolean isOk = problemEntityService.removeById(pid);
        // problem的id为其他表的外键的表中的对应数据都会被一起删除！
        // 删除成功
        if (isOk) {
            FileUtil.del(filePathProps.getTestcaseBaseFolder() + File.separator + "problem_" + pid);
        } else {
            throw new StatusFailException("删除失败！");
        }
    }

    @Override
    public void addProblem(ProblemDto problemDto) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目的Problem ID已存在，请更换！");
        }

        boolean isOk = problemEntityService.adminAddProblem(problemDto);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        String problemId = problemDto.getProblem().getProblemId().toUpperCase();
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemId);
        Problem problem = problemEntityService.getOne(queryWrapper);

        // 如果problem_id不是原来的且已存在该problem_id，则修改失败！
        if (problem != null && problem.getId().longValue() != problemDto.getProblem().getId()) {
            throw new StatusFailException("当前的Problem ID 已被使用，请重新更换新的！");
        }

        // 记录修改题目的用户
        problemDto.getProblem().setModifiedUser(userRolesVo.getUsername());

        boolean result = problemEntityService.adminUpdateProblem(problemDto);
        // 更新成功
        if (result) {
            // 说明改了problemId，同步一下judge表
            if (problem == null) {
                UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                judgeUpdateWrapper.eq("pid", problemDto.getProblem().getId())
                        .set("display_pid", problemId);
                judgeEntityService.update(judgeUpdateWrapper);
            }

        } else {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public List<ProblemCase> getProblemCases(Long pid, Boolean isUpload) {
        QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
        problemCaseQueryWrapper.eq("pid", pid).eq("status", 0);
        if (isUpload) {
            problemCaseQueryWrapper.last("order by length(input) asc,input asc");
        }
        return problemCaseEntityService.list(problemCaseQueryWrapper);
    }

    @Override
    public CommonResult compileSpj(CompileDTO compileDTO) {
        compileDTO.setToken(judgeToken);
        return dispatcher.dispatcher("compile", "/compile-spj", compileDTO);
    }

    @Override
    public CommonResult compileInteractive(CompileDTO compileDTO) {
        compileDTO.setToken(judgeToken);
        return dispatcher.dispatcher("compile", "/compile-interactive", compileDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importRemoteOJProblem(String name, String problemId) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目已添加，请勿重复添加！");
        }

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        try {
            ProblemCrawler.RemoteProblemInfo otherOJProblemInfo = remoteProblemService.getOtherOJProblemInfo(name.toUpperCase(), problemId, userRolesVo.getUsername());
            if (otherOJProblemInfo != null) {
                Problem importProblem = remoteProblemService.adminAddOtherOJProblem(otherOJProblemInfo, name);
                if (importProblem == null) {
                    throw new StatusFailException("导入新题目失败！请重新尝试！");
                }
            } else {
                throw new StatusFailException("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
            }
        } catch (Exception e) {
            throw new StatusFailException(e.getMessage());
        }
    }

    @Override
    public void changeProblemAuth(Problem problem) {
        // 普通管理员只能将题目变成隐藏题目和比赛题目
        boolean root = SecurityUtils.getSubject().hasRole("root");

        boolean problemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");

        if (!problemAdmin && !root && problem.getAuth() == 1) {
            throw new StatusForbiddenException("修改失败！你无权限公开题目！");
        }

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        UpdateWrapper<Problem> problemUpdateWrapper = new UpdateWrapper<>();
        problemUpdateWrapper.eq("id", problem.getId())
                .set("auth", problem.getAuth())
                .set("modified_user", userRolesVo.getUsername());

        boolean isOk = problemEntityService.update(problemUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }


}