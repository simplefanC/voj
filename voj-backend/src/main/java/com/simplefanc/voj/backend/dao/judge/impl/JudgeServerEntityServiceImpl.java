package com.simplefanc.voj.backend.dao.judge.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.judge.JudgeServerEntityService;
import com.simplefanc.voj.backend.mapper.JudgeServerMapper;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeServer;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/4/15 11:27
 * @Description:
 */
@Service
public class JudgeServerEntityServiceImpl extends ServiceImpl<JudgeServerMapper, JudgeServer>
        implements JudgeServerEntityService {

}