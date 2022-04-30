package com.simplefanc.voj.backend.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.problem.CodeTemplate;
import com.simplefanc.voj.backend.dao.problem.CodeTemplateEntityService;
import com.simplefanc.voj.backend.mapper.CodeTemplateMapper;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/4/24 10:27
 * @Description:
 */
@Service
public class CodeTemplateEntityServiceImpl extends ServiceImpl<CodeTemplateMapper, CodeTemplate> implements CodeTemplateEntityService {
}