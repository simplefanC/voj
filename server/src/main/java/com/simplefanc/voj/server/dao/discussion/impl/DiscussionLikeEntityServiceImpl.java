package com.simplefanc.voj.server.dao.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.common.pojo.entity.discussion.DiscussionLike;
import com.simplefanc.voj.server.dao.discussion.DiscussionLikeEntityService;
import com.simplefanc.voj.server.mapper.DiscussionLikeMapper;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2021/5/4 22:31
 * @Description:
 */
@Service
public class DiscussionLikeEntityServiceImpl extends ServiceImpl<DiscussionLikeMapper, DiscussionLike> implements DiscussionLikeEntityService {
}