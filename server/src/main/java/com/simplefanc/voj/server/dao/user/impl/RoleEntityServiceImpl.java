package com.simplefanc.voj.server.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.user.Role;
import com.simplefanc.voj.server.dao.user.RoleEntityService;
import com.simplefanc.voj.server.mapper.RoleMapper;
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
public class RoleEntityServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleEntityService {

}
