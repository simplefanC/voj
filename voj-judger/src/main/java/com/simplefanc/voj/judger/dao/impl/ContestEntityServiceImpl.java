package com.simplefanc.voj.judger.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.judger.dao.ContestEntityService;
import com.simplefanc.voj.judger.mapper.ContestMapper;
import org.springframework.stereotype.Service;

;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author chenfan
 * @since 2020-10-23
 */
@Service
public class ContestEntityServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestEntityService {

}
