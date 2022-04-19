package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.problem.Category;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CategoryMapper extends BaseMapper<Category> {
}
