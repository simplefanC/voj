package com.simplefanc.voj.backend.dao.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.AdminSysNoticeVO;
import com.simplefanc.voj.common.pojo.entity.msg.AdminSysNotice;

/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:33
 * @Description:
 */
public interface AdminSysNoticeEntityService extends IService<AdminSysNotice> {

    IPage<AdminSysNoticeVO> getSysNotice(int limit, int currentPage, String type);

}