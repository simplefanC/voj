package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.pojo.dto.PidListDTO;
import com.simplefanc.voj.backend.pojo.vo.ProblemInfoVO;
import com.simplefanc.voj.backend.pojo.vo.ProblemVO;
import com.simplefanc.voj.backend.pojo.vo.RandomProblemVO;

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
     * @Since 2021/10/27
     */
    Page<ProblemVO> getProblemList(Integer limit, Integer currentPage, String keyword, List<Long> tagId,
                                   Integer difficulty, String oj, Boolean problemVisible);

    /**
     * @MethodName getRandomProblem
     * @Description 随机选取一道题目
     * @Since 2021/10/27
     */
    RandomProblemVO getRandomProblem();

    /**
     * @MethodName getUserProblemStatus
     * @Description 获取用户对应该题目列表中各个题目的做题情况
     * @Since 2021/12/29
     */
    HashMap<Long, Object> getUserProblemStatus(PidListDTO pidListDTO);

    /**
     * @MethodName getProblemInfo
     * @Description 获取指定题目的详情信息，标签，所支持语言，做题情况（只能查询公开题目 也就是auth为1）
     * @Since 2021/10/27
     */
    ProblemInfoVO getProblemInfo(String problemId);

}