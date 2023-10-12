package com.simplefanc.voj.backend.service.admin.training.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingEntityService;
import com.simplefanc.voj.backend.dao.training.TrainingProblemEntityService;
import com.simplefanc.voj.backend.judge.remote.crawler.AbstractProblemCrawler;
import com.simplefanc.voj.backend.pojo.dto.TrainingProblemDTO;
import com.simplefanc.voj.backend.service.admin.problem.RemoteProblemService;
import com.simplefanc.voj.backend.service.admin.training.AdminTrainingProblemService;
import com.simplefanc.voj.backend.service.admin.training.AdminTrainingRecordService;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.common.pojo.entity.training.TrainingProblem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 20:20
 * @Description:
 */
@Service
@RequiredArgsConstructor
@Slf4j(topic = "voj")
public class AdminTrainingProblemServiceImpl implements AdminTrainingProblemService {

    private final TrainingProblemEntityService trainingProblemEntityService;

    private final TrainingEntityService trainingEntityService;

    private final ProblemEntityService problemEntityService;

    private final AdminTrainingRecordService adminTrainingRecordService;

    private final RemoteProblemService remoteProblemService;

    private final FilePathProperties filePathProps;

    @Override
    public HashMap<String, Object> getProblemList(Integer limit, Integer currentPage, String keyword,
                                                  Boolean queryExisted, Long tid) {
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }

        IPage<Problem> iPage = new Page<>(currentPage, limit);
        // 根据tid在TrainingProblem表中查询到对应pid集合
        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid).orderByAsc("display_id");
        List<Long> pidList = new LinkedList<>();
        List<TrainingProblem> trainingProblemList = trainingProblemEntityService.list(trainingProblemQueryWrapper);
        HashMap<Long, TrainingProblem> trainingProblemMap = new HashMap<>();
        trainingProblemList.forEach(trainingProblem -> {
            if (!trainingProblemMap.containsKey(trainingProblem.getPid())) {
                trainingProblemMap.put(trainingProblem.getPid(), trainingProblem);
            }
            pidList.add(trainingProblem.getPid());
        });
        // TODO put 键
        HashMap<String, Object> trainingProblem = new HashMap<>();
        // 该训练原本就无题目数据
        if (pidList.size() == 0 && queryExisted) {
            trainingProblem.put("problemList", pidList);
            trainingProblem.put("contestProblemMap", trainingProblemMap);
            return trainingProblem;
        }

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();

        // 逻辑判断，如果是查询已有的就应该是in，如果是查询不要重复的，使用not in
        if (queryExisted) {
            problemQueryWrapper.in(pidList.size() > 0, "id", pidList);
        } else {
            // 权限需要是公开的（隐藏的，比赛中不可加入！）
            problemQueryWrapper.eq("auth", 1);
            problemQueryWrapper.notIn(pidList.size() > 0, "id", pidList);
        }

        if (StrUtil.isNotEmpty(keyword)) {
            problemQueryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("problem_id", keyword).or()
                    .like("author", keyword));
        }

        IPage<Problem> problemListPager = problemEntityService.page(iPage, problemQueryWrapper);

        if (queryExisted) {
            List<Problem> problemListPagerRecords = problemListPager.getRecords();
            List<Problem> sortProblemList = problemListPagerRecords.stream()
                    .sorted(Comparator.comparingInt(problem -> trainingProblemMap.get(problem.getId()).getRank()))
                    .collect(Collectors.toList());
            problemListPager.setRecords(sortProblemList);
        }
        trainingProblem.put("problemList", problemListPager);
        trainingProblem.put("trainingProblemMap", trainingProblemMap);
        return trainingProblem;
    }

    @Override
    public void updateProblem(TrainingProblem trainingProblem) {
        boolean isOk = trainingProblemEntityService.saveOrUpdate(trainingProblem);

        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }
    }

    @Override
    public void deleteProblem(Long pid, Long tid) {
        boolean isOk;
        // 训练id不为null，表示就是从比赛列表移除而已
        if (tid != null) {
            QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
            trainingProblemQueryWrapper.eq("tid", tid).eq("pid", pid);
            isOk = trainingProblemEntityService.remove(trainingProblemQueryWrapper);
        } else {
            // problem的id为其他表的外键的表中的对应数据都会被一起删除！
            isOk = problemEntityService.removeById(pid);
        }

        if (isOk) {
            if (tid == null) {
                FileUtil.del(filePathProps.getTestcaseBaseFolder() + File.separator + "problem_" + pid);
            }

            // 更新训练最近更新时间
            UpdateWrapper<Training> trainingUpdateWrapper = new UpdateWrapper<>();
            trainingUpdateWrapper.set("gmt_modified", new Date()).eq("id", tid);
            trainingEntityService.update(trainingUpdateWrapper);

        } else {
            throw new StatusFailException("删除失败！");
        }
    }

    @Override
    public void addProblemFromPublic(TrainingProblemDTO trainingProblemDTO) {

        Long pid = trainingProblemDTO.getPid();
        Long tid = trainingProblemDTO.getTid();
        String displayId = trainingProblemDTO.getDisplayId();

        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid)
                .and(wrapper -> wrapper.eq("pid", pid).or().eq("display_id", displayId));
        TrainingProblem trainingProblem = trainingProblemEntityService.getOne(trainingProblemQueryWrapper, false);
        if (trainingProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的训练展示ID已存在！");
        }

        TrainingProblem problem = new TrainingProblem();
        boolean isOk = trainingProblemEntityService
                .saveOrUpdate(problem.setTid(tid).setPid(pid).setDisplayId(displayId));
        if (isOk) {
            // 更新训练最近更新时间
            UpdateWrapper<Training> trainingUpdateWrapper = new UpdateWrapper<>();
            trainingUpdateWrapper.set("gmt_modified", new Date()).eq("id", tid);
            trainingEntityService.update(trainingUpdateWrapper);

            // 异步地同步用户对该题目的提交数据
            adminTrainingRecordService.syncAlreadyRegisterUserRecord(tid, pid, problem.getId());
        } else {
            throw new StatusFailException("添加失败！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTrainingRemoteOjProblem(String name, String problemId, Long tid) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        Problem problem = problemEntityService.getOne(queryWrapper, false);

        // 如果该题目不存在，需要先导入
        if (problem == null) {
            try {
                AbstractProblemCrawler.RemoteProblemInfo otherOjProblemInfo = remoteProblemService
                        .getOtherOJProblemInfo(name.toUpperCase(), problemId);
                if (otherOjProblemInfo != null) {
                    problem = remoteProblemService.adminAddOtherOJProblem(otherOjProblemInfo, name);
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

        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        Problem finalProblem = problem;
        trainingProblemQueryWrapper.eq("tid", tid).and(
                wrapper -> wrapper.eq("pid", finalProblem.getId()).or().eq("display_id", finalProblem.getProblemId()));
        TrainingProblem trainingProblem = trainingProblemEntityService.getOne(trainingProblemQueryWrapper, false);
        if (trainingProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的训练展示ID已存在！");
        }

        TrainingProblem newProblem = new TrainingProblem();
        boolean isOk = trainingProblemEntityService
                .saveOrUpdate(newProblem.setTid(tid).setPid(problem.getId()).setDisplayId(problem.getProblemId()));
        // 添加成功
        if (isOk) {
            // 更新训练最近更新时间
            UpdateWrapper<Training> trainingUpdateWrapper = new UpdateWrapper<>();
            trainingUpdateWrapper.set("gmt_modified", new Date()).eq("id", tid);
            trainingEntityService.update(trainingUpdateWrapper);

            // 异步地同步用户对该题目的提交数据
            adminTrainingRecordService.syncAlreadyRegisterUserRecord(tid, problem.getId(), newProblem.getId());
        } else {
            throw new StatusFailException("添加失败！");
        }
    }

}