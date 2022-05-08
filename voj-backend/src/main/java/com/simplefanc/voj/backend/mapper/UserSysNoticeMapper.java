package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.pojo.vo.SysMsgVo;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserSysNoticeMapper extends BaseMapper<UserSysNotice> {

    IPage<SysMsgVo> getSysOrMineNotice(Page<SysMsgVo> page, @Param("uid") String uid, @Param("type") String type);

}
