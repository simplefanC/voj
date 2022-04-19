package com.simplefanc.voj.judger.dao.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import com.simplefanc.voj.judger.mapper.JudgeMapper;
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
public class JudgeEntityServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeEntityService {

}
