package com.simplefanc.voj.backend.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestExplanationEntityService;
import com.simplefanc.voj.backend.mapper.ContestExplanationMapper;
import com.simplefanc.voj.common.pojo.entity.contest.ContestExplanation;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Service
public class ContestExplanationEntityServiceImpl extends ServiceImpl<ContestExplanationMapper, ContestExplanation>
        implements ContestExplanationEntityService {

}
