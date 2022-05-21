package com.simplefanc.voj.backend.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.problem.ProblemTagEntityService;
import com.simplefanc.voj.backend.mapper.ProblemTagMapper;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemTag;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/12/13 23:22
 * @Description:
 */
@Service
public class ProblemTagEntityServiceImpl extends ServiceImpl<ProblemTagMapper, ProblemTag>
        implements ProblemTagEntityService {

}