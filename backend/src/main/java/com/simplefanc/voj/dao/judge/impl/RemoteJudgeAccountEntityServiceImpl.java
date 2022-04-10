package com.simplefanc.voj.dao.judge.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.judge.RemoteJudgeAccountEntityService;
import com.simplefanc.voj.mapper.RemoteJudgeAccountMapper;
import com.simplefanc.voj.pojo.entity.judge.RemoteJudgeAccount;


/**
 * @Author: chenfan
 * @Date: 2021/5/18 17:46
 * @Description:
 */
@Service
public class RemoteJudgeAccountEntityServiceImpl extends ServiceImpl<RemoteJudgeAccountMapper, RemoteJudgeAccount> implements RemoteJudgeAccountEntityService {
}