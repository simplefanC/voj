package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.user.Role;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

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
public interface RoleMapper extends BaseMapper<Role> {

}
