package com.simplefanc.voj.backend.dao.problem;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.dto.ProblemDTO;
import com.simplefanc.voj.backend.pojo.vo.ImportProblemVO;
import com.simplefanc.voj.backend.pojo.vo.ProblemVO;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */

public interface ProblemEntityService extends IService<Problem> {

    Page<ProblemVO> getProblemList(int limit, int currentPage, String title, Integer difficulty,
                                   List<Long> tagIds, String oj, boolean isAdmin);

    boolean adminUpdateProblem(ProblemDTO problemDTO);

    boolean adminAddProblem(ProblemDTO problemDTO);

    ImportProblemVO buildExportProblem(Long pid, List<HashMap<String, Object>> problemCaseList,
                                       HashMap<Long, String> languageMap, HashMap<Long, String> tagMap);

}
