package com.simplefanc.voj.judger.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.judger.dao.ProblemEntityService;
import com.simplefanc.voj.judger.mapper.ProblemMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author chenfan
 * @since 2021-10-23
 */
@Service
public class ProblemEntityServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemEntityService {

}
