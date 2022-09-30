package com.simplefanc.voj.backend.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.ContestRankDto;
import com.simplefanc.voj.backend.pojo.vo.ContestOutsideInfo;
import com.simplefanc.voj.backend.service.oj.ContestScoreboardService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 22:11
 * @Description: 处理比赛外榜的相关请求
 */

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContestScoreboardController {

    private final ContestScoreboardService contestScoreboardService;

    /**
     * @param cid 比赛id
     * @MethodName getContestOutsideInfo
     * @Description 提供比赛外榜所需的比赛信息和题目信息
     * @Return
     * @Since 2021/12/8
     */
    @GetMapping("/get-contest-outsize-info")
    public CommonResult<ContestOutsideInfo> getContestOutsideInfo(
            @RequestParam(value = "cid") Long cid) {
        return CommonResult.successResponse(contestScoreboardService.getContestOutsideInfo(cid));
    }

    /**
     * @MethodName getContestScoreBoard
     * @Description 提供比赛外榜排名数据
     * @Return
     * @Since 2021/12/07
     */
    @PostMapping("/get-contest-outside-scoreboard")
    public CommonResult<IPage> getContestOutsideScoreboard(@RequestBody ContestRankDto contestRankDto) {
        return CommonResult.successResponse(contestScoreboardService.getContestOutsideScoreboard(contestRankDto));
    }

}