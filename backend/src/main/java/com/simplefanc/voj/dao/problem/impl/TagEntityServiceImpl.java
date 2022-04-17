package com.simplefanc.voj.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.problem.TagEntityService;
import com.simplefanc.voj.mapper.TagMapper;
import com.simplefanc.voj.pojo.entity.problem.Tag;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Service
public class TagEntityServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagEntityService {

}
