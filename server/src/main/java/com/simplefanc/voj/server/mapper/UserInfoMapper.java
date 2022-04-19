package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import com.simplefanc.voj.server.pojo.dto.RegisterDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    int addUser(RegisterDto registerDto);

    List<String> getSuperAdminUidList();
}
