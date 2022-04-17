package com.simplefanc.voj.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.contest.ContestScoreEntityService;
import com.simplefanc.voj.mapper.ContestScoreMapper;
import com.simplefanc.voj.pojo.entity.contest.ContestScore;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Service
public class ContestScoreEntityServiceImpl extends ServiceImpl<ContestScoreMapper, ContestScore> implements ContestScoreEntityService {

}
