package com.simplefanc.voj.backend.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestRegisterEntityService;
import com.simplefanc.voj.backend.mapper.ContestRegisterMapper;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRegister;
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
public class ContestRegisterEntityServiceImpl extends ServiceImpl<ContestRegisterMapper, ContestRegister>
        implements ContestRegisterEntityService {

}
