package com.simplefanc.voj.backend.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.common.constants.RoleEnum;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.mapper.UserInfoMapper;
import com.simplefanc.voj.backend.pojo.dto.RegisterDto;
import com.simplefanc.voj.common.constants.RedisConstant;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Service
@RequiredArgsConstructor
public class UserInfoEntityServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoEntityService {

    private final UserInfoMapper userInfoMapper;

    private final RedisUtil redisUtil;

    @Override
    public Boolean addUser(RegisterDto registerDto) {
        return userInfoMapper.addUser(registerDto) == 1;
    }

    @Override
    @Cacheable(value = RedisConstant.SUPER_ADMIN_UID_LIST_CACHE)
    public List<String> getSuperAdminUidList() {
//        List<String> superAdminUidList = (List<String>) redisUtil.get(AccountConstant.SUPER_ADMIN_UID_LIST_CACHE);
//        if (superAdminUidList == null) {
//            superAdminUidList = userInfoMapper.getSuperAdminUidList(RoleEnum.ROOT.getId());
//            redisUtil.set(AccountConstant.SUPER_ADMIN_UID_LIST_CACHE, superAdminUidList, 12 * 3600);
//        }
//        return superAdminUidList;
        return userInfoMapper.getSuperAdminUidList(RoleEnum.ROOT.getId());
    }

}
