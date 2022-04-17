package com.simplefanc.voj.dao.judge.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.judge.JudgeServerEntityService;
import com.simplefanc.voj.mapper.JudgeServerMapper;
import com.simplefanc.voj.pojo.entity.judge.JudgeServer;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/4/15 11:27
 * @Description:
 */
@Service
public class JudgeServerEntityServiceImpl extends ServiceImpl<JudgeServerMapper, JudgeServer> implements JudgeServerEntityService {

}