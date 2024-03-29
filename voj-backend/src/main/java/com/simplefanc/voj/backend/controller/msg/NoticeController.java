package com.simplefanc.voj.backend.controller.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.SysMsgVO;
import com.simplefanc.voj.backend.service.msg.NoticeService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: chenfan
 * @Date: 2021/10/1 20:42
 * @Description: 负责用户的 系统消息模块、我的消息模块
 */
@RestController
@RequestMapping("/api/msg")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @RequestMapping(value = "/sys", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult<IPage<SysMsgVO>> getSysNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                                      @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return CommonResult.successResponse(noticeService.getSysNotice(limit, currentPage));
    }

    @RequestMapping(value = "/mine", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult<IPage<SysMsgVO>> getMineNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                                       @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return CommonResult.successResponse(noticeService.getMineNotice(limit, currentPage));
    }

}