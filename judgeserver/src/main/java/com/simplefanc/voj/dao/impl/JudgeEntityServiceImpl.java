package com.simplefanc.voj.dao.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.JudgeEntityService;
import com.simplefanc.voj.mapper.JudgeMapper;
import com.simplefanc.voj.pojo.entity.judge.Judge;
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
