package com.simplefanc.voj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.pojo.vo.SysMsgVo;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 11:37
 * @Description:
 */

public interface NoticeService {

    IPage<SysMsgVo> getSysNotice(Integer limit, Integer currentPage);

    IPage<SysMsgVo> getMineNotice(Integer limit, Integer currentPage);

    void updateSysOrMineMsgRead(IPage<SysMsgVo> userMsgList);

    void syncNoticeToNewRegisterUser(String uid);
}