package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.pojo.entity.contest.ContestPrint;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author: chenfan
 * @Date: 2021/9/19 21:04
 * @Description:
 */
@Mapper
@Repository
public interface ContestPrintMapper extends BaseMapper<ContestPrint> {
}