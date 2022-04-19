package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;
import com.simplefanc.voj.server.pojo.vo.SysMsgVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface UserSysNoticeMapper extends BaseMapper<UserSysNotice> {

    IPage<SysMsgVo> getSysOrMineNotice(Page<SysMsgVo> page, @Param("uid") String uid, @Param("type") String type);
}
