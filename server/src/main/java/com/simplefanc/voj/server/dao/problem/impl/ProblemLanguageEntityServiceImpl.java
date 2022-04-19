package com.simplefanc.voj.server.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemLanguage;
import com.simplefanc.voj.server.dao.problem.ProblemLanguageEntityService;
import com.simplefanc.voj.server.mapper.ProblemLanguageMapper;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2020/12/13 00:04
 * @Description:
 */
@Service
public class ProblemLanguageEntityServiceImpl extends ServiceImpl<ProblemLanguageMapper, ProblemLanguage> implements ProblemLanguageEntityService {
}