package com.simplefanc.voj.backend.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.user.RoleAuthEntityService;
import com.simplefanc.voj.backend.mapper.RoleAuthMapper;
import com.simplefanc.voj.common.pojo.entity.user.RoleAuth;
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
public class RoleAuthEntityServiceImpl extends ServiceImpl<RoleAuthMapper, RoleAuth> implements RoleAuthEntityService {

}
