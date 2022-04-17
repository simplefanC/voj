package com.simplefanc.voj.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.user.UserInfoEntityService;
import com.simplefanc.voj.mapper.UserInfoMapper;
import com.simplefanc.voj.pojo.dto.RegisterDto;
import com.simplefanc.voj.pojo.entity.user.UserInfo;
import com.simplefanc.voj.utils.Constants;
import com.simplefanc.voj.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Service
public class UserInfoEntityServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoEntityService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Boolean addUser(RegisterDto registerDto) {
        return userInfoMapper.addUser(registerDto) == 1;
    }

    @Override
    public List<String> getSuperAdminUidList() {
        String cacheKey = Constants.Account.SUPER_ADMIN_UID_LIST_CACHE.getCode();
        List<String> superAdminUidList = (List<String>) redisUtils.get(cacheKey);
        if (superAdminUidList == null) {
            superAdminUidList = userInfoMapper.getSuperAdminUidList();
            redisUtils.set(cacheKey, superAdminUidList, 12 * 3600);
        }
        return superAdminUidList;
    }

}
