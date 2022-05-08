package com.simplefanc.voj.backend.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.CheckAcDto;
import com.simplefanc.voj.backend.service.oj.ContestAdminService;
import com.simplefanc.voj.common.pojo.entity.contest.ContestPrint;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRecord;
import com.simplefanc.voj.common.result.CommonResult;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: chenfan
 * @Date: 2021/9/20 13:15
 * @Description: 处理比赛管理模块的相关数据请求
 */
@RestController
@RequestMapping("/api")
public class ContestAdminController {

    @Autowired
    private ContestAdminService contestAdminService;

    /**
     * @MethodName getContestACInfo
     * @Params * @param null
     * @Description 获取各个用户的ac情况，仅限于比赛管理者可查看
     * @Return
     * @Since 2021/1/17
     */
    @GetMapping("/get-contest-ac-info")
    @RequiresAuthentication
    public CommonResult<IPage<ContestRecord>> getContestACInfo(@RequestParam("cid") Long cid,
                                                               @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                               @RequestParam(value = "limit", required = false) Integer limit) {
        return CommonResult.successResponse(contestAdminService.getContestACInfo(cid, currentPage, limit));
    }

    /**
     * @MethodName checkContestACInfo
     * @Params * @param null
     * @Description 比赛管理员确定该次提交的ac情况
     * @Return
     * @Since 2021/1/17
     */
    @PutMapping("/check-contest-ac-info")
    @RequiresAuthentication
    public CommonResult<Void> checkContestACInfo(@RequestBody CheckAcDto checkACDto) {
        contestAdminService.checkContestAcInfo(checkACDto);
        return CommonResult.successResponse();
    }

    @GetMapping("/get-contest-print")
    @RequiresAuthentication
    public CommonResult<IPage<ContestPrint>> getContestPrint(@RequestParam("cid") Long cid,
                                                             @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                             @RequestParam(value = "limit", required = false) Integer limit) {
        return CommonResult.successResponse(contestAdminService.getContestPrint(cid, currentPage, limit));
    }

    /**
     * @param id
     * @param cid
     * @MethodName checkContestStatus
     * @Description 更新该打印为确定状态
     * @Return
     * @Since 2021/9/20
     */
    @PutMapping("/check-contest-print-status")
    @RequiresAuthentication
    public CommonResult<Void> checkContestPrintStatus(@RequestParam("id") Long id, @RequestParam("cid") Long cid) {
        contestAdminService.checkContestPrintStatus(id, cid);
        return CommonResult.successResponse();
    }

}