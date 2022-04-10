package com.simplefanc.voj.dao.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.ProblemEntityService;
import com.simplefanc.voj.mapper.ProblemMapper;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author chenfan
 * @since 2020-10-23
 */
@Service
public class ProblemEntityServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemEntityService {

}
