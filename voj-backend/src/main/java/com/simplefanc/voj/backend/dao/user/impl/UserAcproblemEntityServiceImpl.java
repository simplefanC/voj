package com.simplefanc.voj.backend.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.user.UserAcproblemEntityService;
import com.simplefanc.voj.backend.mapper.UserAcproblemMapper;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
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
public class UserAcproblemEntityServiceImpl extends ServiceImpl<UserAcproblemMapper, UserAcproblem>
        implements UserAcproblemEntityService {

}
