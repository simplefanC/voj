package com.simplefanc.voj.backend.dao.contest;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRegister;

import java.util.Set;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
public interface ContestRegisterEntityService extends IService<ContestRegister> {
    Set<String> getRegisteredUsers(Long cid);
}
