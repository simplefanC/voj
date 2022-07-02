package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.user.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

}
