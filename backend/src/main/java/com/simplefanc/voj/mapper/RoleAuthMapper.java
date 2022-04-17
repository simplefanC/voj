package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.pojo.entity.user.RoleAuth;
import com.simplefanc.voj.pojo.vo.RoleAuthsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
public interface RoleAuthMapper extends BaseMapper<RoleAuth> {
    RoleAuthsVo getRoleAuths(@Param("rid") long rid);
}
