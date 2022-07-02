package com.simplefanc.voj.backend.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestRegisterEntityService;
import com.simplefanc.voj.backend.mapper.ContestRegisterMapper;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRegister;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Service
public class ContestRegisterEntityServiceImpl extends ServiceImpl<ContestRegisterMapper, ContestRegister>
        implements ContestRegisterEntityService {

    @Override
    public Set<String> getRegisteredUsers(Long cid) {
        return this.lambdaQuery()
                .select(ContestRegister::getUid)
                .eq(ContestRegister::getCid, cid)
                .list()
                .stream()
                .map(ContestRegister::getUid)
                .collect(Collectors.toSet());
    }
}
