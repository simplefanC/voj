package com.simplefanc.voj.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.problem.ProblemLanguageEntityService;
import com.simplefanc.voj.mapper.ProblemLanguageMapper;
import com.simplefanc.voj.pojo.entity.problem.ProblemLanguage;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2020/12/13 00:04
 * @Description:
 */
@Service
public class ProblemLanguageEntityServiceImpl extends ServiceImpl<ProblemLanguageMapper, ProblemLanguage> implements ProblemLanguageEntityService {
}