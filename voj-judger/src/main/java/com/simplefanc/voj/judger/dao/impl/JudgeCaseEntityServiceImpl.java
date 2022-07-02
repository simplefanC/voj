package com.simplefanc.voj.judger.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeCase;
import com.simplefanc.voj.judger.dao.JudgeCaseEntityService;
import com.simplefanc.voj.judger.mapper.JudgeCaseMapper;
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
public class JudgeCaseEntityServiceImpl extends ServiceImpl<JudgeCaseMapper, JudgeCase>
        implements JudgeCaseEntityService {

}
