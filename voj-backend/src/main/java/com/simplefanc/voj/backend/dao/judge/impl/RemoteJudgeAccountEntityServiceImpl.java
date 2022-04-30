package com.simplefanc.voj.backend.dao.judge.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.judge.RemoteJudgeAccount;
import com.simplefanc.voj.backend.dao.judge.RemoteJudgeAccountEntityService;
import com.simplefanc.voj.backend.mapper.RemoteJudgeAccountMapper;
import org.springframework.stereotype.Service;


/**
 * @Author: chenfan
 * @Date: 2021/5/18 17:46
 * @Description:
 */
@Service
public class RemoteJudgeAccountEntityServiceImpl extends ServiceImpl<RemoteJudgeAccountMapper, RemoteJudgeAccount> implements RemoteJudgeAccountEntityService {
}