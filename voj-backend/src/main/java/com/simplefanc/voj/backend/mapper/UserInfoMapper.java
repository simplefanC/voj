package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.backend.pojo.dto.RegisterDto;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    int addUser(RegisterDto registerDto);

    List<String> getSuperAdminUidList(@Param("roleId") Long roleId);

}
