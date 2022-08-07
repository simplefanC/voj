package com.simplefanc.voj.backend.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.problem.TagClassificationEntityService;
import com.simplefanc.voj.backend.mapper.TagClassificationMapper;
import com.simplefanc.voj.common.pojo.entity.problem.TagClassification;
import org.springframework.stereotype.Service;

/**
 * @Author chenfan
 * @Date 2022/8/3
 */
@Service
public class TagClassificationEntityServiceImpl extends ServiceImpl<TagClassificationMapper, TagClassification> implements TagClassificationEntityService {
}
