package com.simplefanc.voj.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.user.RoleAuthEntityService;
import com.simplefanc.voj.mapper.RoleAuthMapper;
import com.simplefanc.voj.pojo.entity.user.RoleAuth;
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
public class RoleAuthEntityServiceImpl extends ServiceImpl<RoleAuthMapper, RoleAuth> implements RoleAuthEntityService {

}
