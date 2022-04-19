package com.simplefanc.voj.server.service.oj;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.server.pojo.dto.PidListDto;
import com.simplefanc.voj.server.pojo.vo.ProblemInfoVo;
import com.simplefanc.voj.server.pojo.vo.ProblemVo;
import com.simplefanc.voj.server.pojo.vo.RandomProblemVo;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 10:37
 * @Description:
 */

public interface ProblemService {


    /**
     * @MethodName getProblemList
     * @Params * @param null
     * @Description 获取题目列表分页
     * @Since 2020/10/27
     */
    Page<ProblemVo> getProblemList(Integer limit, Integer currentPage,
                                   String keyword, List<Long> tagId, Integer difficulty, String oj);

    /**
     * @MethodName getRandomProblem
     * @Description 随机选取一道题目
     * @Since 2020/10/27
     */
    RandomProblemVo getRandomProblem();

    /**
     * @MethodName getUserProblemStatus
     * @Description 获取用户对应该题目列表中各个题目的做题情况
     * @Since 2020/12/29
     */
    HashMap<Long, Object> getUserProblemStatus(PidListDto pidListDto);

    /**
     * @MethodName getProblemInfo
     * @Description 获取指定题目的详情信息，标签，所支持语言，做题情况（只能查询公开题目 也就是auth为1）
     * @Since 2020/10/27
     */
    ProblemInfoVo getProblemInfo(String problemId);
}