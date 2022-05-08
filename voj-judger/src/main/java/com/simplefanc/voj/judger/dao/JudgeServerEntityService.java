package com.simplefanc.voj.judger.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeServer;

import java.util.HashMap;

public interface JudgeServerEntityService extends IService<JudgeServer> {

    HashMap<String, Object> getJudgeServerInfo();

}
