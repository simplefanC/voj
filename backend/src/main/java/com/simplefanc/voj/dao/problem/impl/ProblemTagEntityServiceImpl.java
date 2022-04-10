package com.simplefanc.voj.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.problem.ProblemTagEntityService;
import com.simplefanc.voj.mapper.ProblemTagMapper;
import com.simplefanc.voj.pojo.entity.problem.ProblemTag;

/**
 * @Author: chenfan
 * @Date: 2020/12/13 23:22
 * @Description:
 */
@Service
public class ProblemTagEntityServiceImpl extends ServiceImpl<ProblemTagMapper, ProblemTag> implements ProblemTagEntityService {
}