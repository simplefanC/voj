package com.simplefanc.voj.dao.problem;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.dto.ProblemDto;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.pojo.vo.ImportProblemVo;
import com.simplefanc.voj.pojo.vo.ProblemVo;

import java.util.HashMap;
import java.util.List;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */

public interface ProblemEntityService extends IService<Problem> {
    Page<ProblemVo> getProblemList(int limit, int currentPage, Long pid, String title,
                                   Integer difficulty, List<Long> tid, String oj);

    boolean adminUpdateProblem(ProblemDto problemDto);

    boolean adminAddProblem(ProblemDto problemDto);

    ImportProblemVo buildExportProblem(Long pid, List<HashMap<String, Object>> problemCaseList, HashMap<Long, String> languageMap, HashMap<Long, String> tagMap);
}
