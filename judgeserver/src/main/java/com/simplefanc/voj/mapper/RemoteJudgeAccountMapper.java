package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.pojo.entity.judge.RemoteJudgeAccount;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface RemoteJudgeAccountMapper extends BaseMapper<RemoteJudgeAccount> {
}
