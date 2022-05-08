package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.pojo.vo.AdminSysNoticeVo;
import com.simplefanc.voj.common.pojo.entity.msg.AdminSysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminSysNoticeMapper extends BaseMapper<AdminSysNotice> {

    IPage<AdminSysNoticeVo> getAdminSysNotice(Page<AdminSysNoticeVo> page, @Param("type") String type);

}
