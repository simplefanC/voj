package com.simplefanc.voj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.simplefanc.voj.pojo.entity.msg.UserSysNotice;
import com.simplefanc.voj.pojo.vo.SysMsgVo;


@Mapper
@Repository
public interface UserSysNoticeMapper extends BaseMapper<UserSysNotice> {

    IPage<SysMsgVo> getSysOrMineNotice(Page<SysMsgVo> page, @Param("uid") String uid, @Param("type") String type);
}
