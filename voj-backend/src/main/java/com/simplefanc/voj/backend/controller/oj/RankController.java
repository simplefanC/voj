package com.simplefanc.voj.backend.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.service.oj.RankService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: chenfan
 * @Date: 2021/10/27 20:53
 * @Description: 处理排行榜数据
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    /**
     * @MethodName get-rank-list
     * @Params * @param null
     * @Description 获取排行榜数据
     * @Return CommonResult
     * @Since 2021/10/27
     */
    @GetMapping("/get-rank-list")
    public CommonResult<IPage> getRankList(@RequestParam(value = "limit", required = false) Integer limit,
                                           @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                           @RequestParam(value = "searchUser", required = false) String searchUser,
                                           @RequestParam(value = "type") Integer type) {
        return CommonResult.successResponse(rankService.getRankList(limit, currentPage, searchUser, type));
    }

}