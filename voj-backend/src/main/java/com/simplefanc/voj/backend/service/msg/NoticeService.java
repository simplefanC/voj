package com.simplefanc.voj.backend.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.SysMsgVO;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 11:37
 * @Description:
 */

public interface NoticeService {

    IPage<SysMsgVO> getSysNotice(Integer limit, Integer currentPage);

    IPage<SysMsgVO> getMineNotice(Integer limit, Integer currentPage);

    void updateSysOrMineMsgRead(IPage<SysMsgVO> userMsgList);

    void syncNoticeToNewRegisterUser(String uid);

}