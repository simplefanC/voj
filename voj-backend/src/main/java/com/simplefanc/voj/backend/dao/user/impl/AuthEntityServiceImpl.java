package com.simplefanc.voj.backend.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.user.AuthEntityService;
import com.simplefanc.voj.backend.mapper.AuthMapper;
import com.simplefanc.voj.common.pojo.entity.user.Auth;
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
public class AuthEntityServiceImpl extends ServiceImpl<AuthMapper, Auth> implements AuthEntityService {

}
