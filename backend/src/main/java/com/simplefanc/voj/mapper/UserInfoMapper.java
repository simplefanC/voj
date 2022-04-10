package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import com.simplefanc.voj.pojo.dto.RegisterDto;
import com.simplefanc.voj.pojo.entity.user.UserInfo;

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
