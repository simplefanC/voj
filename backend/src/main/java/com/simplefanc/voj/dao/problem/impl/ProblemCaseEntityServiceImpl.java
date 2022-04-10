package com.simplefanc.voj.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.problem.ProblemCaseEntityService;
import com.simplefanc.voj.mapper.ProblemCaseMapper;
import com.simplefanc.voj.pojo.entity.problem.ProblemCase;

/**
 * @Author: chenfan
 * @Date: 2020/12/14 19:59
 * @Description:
 */
@Service
public class ProblemCaseEntityServiceImpl extends ServiceImpl<ProblemCaseMapper, ProblemCase> implements ProblemCaseEntityService {
}