package com.simplefanc.voj.server.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import com.simplefanc.voj.server.dao.problem.TagEntityService;
import com.simplefanc.voj.server.mapper.TagMapper;
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
