package com.simplefanc.voj.backend.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.problem.LanguageEntityService;
import com.simplefanc.voj.backend.mapper.LanguageMapper;
import com.simplefanc.voj.common.pojo.entity.problem.Language;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2020/12/12 23:23
 * @Description:
 */
@Service
public class LanguageEntityServiceImpl extends ServiceImpl<LanguageMapper, Language> implements LanguageEntityService {

}