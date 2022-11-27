package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.backend.pojo.vo.RoleAuthsVO;
import com.simplefanc.voj.common.pojo.entity.user.RoleAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Mapper
public interface RoleAuthMapper extends BaseMapper<RoleAuth> {

    RoleAuthsVO getRoleAuths(@Param("rid") long rid);

}
