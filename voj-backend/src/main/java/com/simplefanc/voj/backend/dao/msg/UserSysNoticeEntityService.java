package com.simplefanc.voj.backend.dao.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.SysMsgVO;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;

public interface UserSysNoticeEntityService extends IService<UserSysNotice> {

    IPage<SysMsgVO> getSysNotice(int limit, int currentPage, String uid);

    IPage<SysMsgVO> getMineNotice(int limit, int currentPage, String uid);

}