package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.judge.RemoteJudgeAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RemoteJudgeAccountMapper extends BaseMapper<RemoteJudgeAccount> {

    @Select("select * from `remote_judge_account` where `oj` = #{oj} and `status` = 1 for update")
    List<RemoteJudgeAccount> getAvailableAccount(@Param("oj") String oj);

    @Update("update `remote_judge_account` set `status` = 0 where `id` = #{id} and `status` = 1")
    int updateAccountStatusById(@Param("id") Integer id);

}
