package com.simplefanc.voj.server.dao.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.msg.AdminSysNotice;
import com.simplefanc.voj.server.pojo.vo.AdminSysNoticeVo;


/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:33
 * @Description:
 */
public interface AdminSysNoticeEntityService extends IService<AdminSysNotice> {

    IPage<AdminSysNoticeVo> getSysNotice(int limit, int currentPage, String type);

}