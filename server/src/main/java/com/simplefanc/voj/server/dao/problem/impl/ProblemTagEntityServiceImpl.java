package com.simplefanc.voj.server.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemTag;
import com.simplefanc.voj.server.dao.problem.ProblemTagEntityService;
import com.simplefanc.voj.server.mapper.ProblemTagMapper;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2020/12/13 23:22
 * @Description:
 */
@Service
public class ProblemTagEntityServiceImpl extends ServiceImpl<ProblemTagMapper, ProblemTag> implements ProblemTagEntityService {
}