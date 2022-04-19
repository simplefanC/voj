package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import com.simplefanc.voj.server.pojo.vo.UserMsgVo;
import com.simplefanc.voj.server.pojo.vo.UserUnreadMsgCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface MsgRemindMapper extends BaseMapper<MsgRemind> {
    UserUnreadMsgCountVo getUserUnreadMsgCount(@Param("uid") String uid);

    IPage<UserMsgVo> getUserMsg(Page<UserMsgVo> page, @Param("uid") String uid,
                                @Param("action") String action);
}
