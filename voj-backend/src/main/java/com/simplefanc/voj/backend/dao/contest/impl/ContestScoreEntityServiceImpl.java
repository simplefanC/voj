package com.simplefanc.voj.backend.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestScoreEntityService;
import com.simplefanc.voj.backend.mapper.ContestScoreMapper;
import com.simplefanc.voj.common.pojo.entity.contest.ContestScore;
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
public class ContestScoreEntityServiceImpl extends ServiceImpl<ContestScoreMapper, ContestScore>
        implements ContestScoreEntityService {

}
