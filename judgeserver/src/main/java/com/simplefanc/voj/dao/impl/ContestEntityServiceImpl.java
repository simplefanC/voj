package com.simplefanc.voj.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.ContestEntityService;
import com.simplefanc.voj.mapper.ContestMapper;
import com.simplefanc.voj.pojo.entity.contest.Contest;
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
