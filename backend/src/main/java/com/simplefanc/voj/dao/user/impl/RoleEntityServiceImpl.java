package com.simplefanc.voj.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.user.RoleEntityService;
import com.simplefanc.voj.mapper.RoleMapper;
import com.simplefanc.voj.pojo.entity.user.Role;

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
