package com.simplefanc.voj.dao.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.judge.JudgeEntityService;
import com.simplefanc.voj.dao.training.TrainingProblemEntityService;
import com.simplefanc.voj.mapper.TrainingProblemMapper;
import com.simplefanc.voj.pojo.entity.judge.Judge;
import com.simplefanc.voj.pojo.entity.training.TrainingProblem;
import com.simplefanc.voj.pojo.vo.ProblemVo;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2021/11/20 12:25
 * @Description:
 */
@Service
public class TrainingProblemEntityServiceImpl extends ServiceImpl<TrainingProblemMapper, TrainingProblem> implements TrainingProblemEntityService {

    @Resource
    private TrainingProblemMapper trainingProblemMapper;

    @Resource
    private JudgeEntityService judgeEntityService;

    static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public List<Long> getTrainingProblemIdList(Long tid) {
        return trainingProblemMapper.getTrainingProblemCount(tid);
    }

    @Override
    public List<ProblemVo> getTrainingProblemList(Long tid) {
        List<ProblemVo> trainingProblemList = trainingProblemMapper.getTrainingProblemList(tid);
        return trainingProblemList.stream().filter(distinctByKey(ProblemVo::getPid)).collect(Collectors.toList());
    }

    @Override
    public Integer getUserTrainingACProblemCount(String uid, List<Long> pidList) {
        if (CollectionUtils.isEmpty(pidList)) {
            return 0;
        }
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("DISTINCT pid")
                .in("pid", pidList)
                .eq("cid", 0)
                .eq("uid", uid)
                .eq("status", 0);
        return judgeEntityService.count(judgeQueryWrapper);
    }

}