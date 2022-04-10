package com.simplefanc.voj.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.contest.ContestRegisterEntityService;
import com.simplefanc.voj.mapper.ContestRegisterMapper;
import com.simplefanc.voj.pojo.entity.contest.ContestRegister;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Service
public class ContestRegisterEntityServiceImpl extends ServiceImpl<ContestRegisterMapper, ContestRegister> implements ContestRegisterEntityService {

}
