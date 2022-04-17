package com.simplefanc.voj.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.judge.JudgeServer;

import java.util.HashMap;

public interface JudgeServerEntityService extends IService<JudgeServer> {

    HashMap<String, Object> getJudgeServerInfo();
}
