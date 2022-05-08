package com.simplefanc.voj.judger.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import com.simplefanc.voj.judger.dao.ProblemCaseEntityService;
import com.simplefanc.voj.judger.mapper.ProblemCaseMapper;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2020/12/14 19:59
 * @Description:
 */
@Service
public class ProblemCaseEntityServiceImpl extends ServiceImpl<ProblemCaseMapper, ProblemCase>
        implements ProblemCaseEntityService {

}