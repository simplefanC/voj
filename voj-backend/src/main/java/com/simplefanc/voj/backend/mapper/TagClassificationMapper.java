package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.problem.TagClassification;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author chenfan
 * @Date 2022/8/3
 */
@Mapper
public interface TagClassificationMapper extends BaseMapper<TagClassification> {
}
