package com.simplefanc.voj.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.ProblemCaseEntityService;
import com.simplefanc.voj.mapper.ProblemCaseMapper;
import com.simplefanc.voj.pojo.entity.problem.ProblemCase;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2020/12/14 19:59
 * @Description:
 */
@Service
public class ProblemCaseEntityServiceImpl extends ServiceImpl<ProblemCaseMapper, ProblemCase> implements ProblemCaseEntityService {
}