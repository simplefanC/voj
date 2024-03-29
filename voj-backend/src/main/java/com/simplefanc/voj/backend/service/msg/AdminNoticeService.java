package com.simplefanc.voj.backend.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.AdminSysNoticeVO;
import com.simplefanc.voj.common.pojo.entity.msg.AdminSysNotice;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:19
 * @Description:
 */
public interface AdminNoticeService {

    IPage<AdminSysNoticeVO> getSysNotice(Integer limit, Integer currentPage, String type);

    void addSysNotice(AdminSysNotice adminSysNotice);

    void deleteSysNotice(Long id);

    void updateSysNotice(AdminSysNotice adminSysNotice);

    void syncNoticeToNewRegisterBatchUser(List<String> uidList);

    void addSingleNoticeToUser(String adminId, String recipientId, String title, String content, String type);

}