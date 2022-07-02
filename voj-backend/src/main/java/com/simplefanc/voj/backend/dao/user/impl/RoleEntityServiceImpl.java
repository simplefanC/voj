package com.simplefanc.voj.backend.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.user.RoleEntityService;
import com.simplefanc.voj.backend.mapper.RoleMapper;
import com.simplefanc.voj.common.pojo.entity.user.Role;
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
public class RoleEntityServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleEntityService {

}
