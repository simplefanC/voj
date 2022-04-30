package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: chenfan
 * @Date: 2021/5/5 22:07
 * @Description:
 */

@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {
}