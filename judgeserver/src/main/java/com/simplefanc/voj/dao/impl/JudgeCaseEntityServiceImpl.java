package com.simplefanc.voj.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.JudgeCaseEntityService;
import com.simplefanc.voj.mapper.JudgeCaseMapper;
import com.simplefanc.voj.pojo.entity.judge.JudgeCase;
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
public class JudgeCaseEntityServiceImpl extends ServiceImpl<JudgeCaseMapper, JudgeCase> implements JudgeCaseEntityService {

}
