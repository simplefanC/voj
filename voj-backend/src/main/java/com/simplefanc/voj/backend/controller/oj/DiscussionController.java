package com.simplefanc.voj.backend.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.DiscussionVo;
import com.simplefanc.voj.backend.service.oj.DiscussionService;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.discussion.DiscussionReport;
import com.simplefanc.voj.common.pojo.entity.problem.Category;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/05/04 10:14
 * @Description: 负责讨论与评论模块的数据接口
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping("/discussions")
    public CommonResult<IPage<Discussion>> getDiscussionList(
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit,
            @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
            @RequestParam(value = "cid", required = false) Integer categoryId,
            @RequestParam(value = "pid", required = false) String pid,
            @RequestParam(value = "onlyMine", required = false, defaultValue = "false") Boolean onlyMine,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "admin", defaultValue = "false") Boolean admin) {

        return CommonResult.successResponse(
                discussionService.getDiscussionList(limit, currentPage, categoryId, pid, onlyMine, keyword, admin));

    }

    @GetMapping("/discussion")
    public CommonResult<DiscussionVo> getDiscussion(@RequestParam(value = "did") Integer did) {
        return CommonResult.successResponse(discussionService.getDiscussion(did));
    }

    @PostMapping("/discussion")
    @RequiresPermissions("discussion_add")
    @RequiresAuthentication
    public CommonResult<Void> addDiscussion(@RequestBody Discussion discussion) {
        discussionService.addDiscussion(discussion);
        return CommonResult.successResponse();
    }

    @PutMapping("/discussion")
    @RequiresPermissions("discussion_edit")
    @RequiresAuthentication
    public CommonResult<Void> updateDiscussion(@RequestBody Discussion discussion) {
        discussionService.updateDiscussion(discussion);
        return CommonResult.successResponse();
    }

    @DeleteMapping("/discussion")
    @RequiresPermissions("discussion_del")
    @RequiresAuthentication
    public CommonResult<Void> removeDiscussion(@RequestParam("did") Integer did) {
        discussionService.removeDiscussion(did);
        return CommonResult.successResponse();
    }

    @GetMapping("/discussion-like")
    @RequiresAuthentication
    public CommonResult<Void> addDiscussionLike(@RequestParam("did") Integer did,
                                                @RequestParam("toLike") Boolean toLike) {
        discussionService.addDiscussionLike(did, toLike);
        return CommonResult.successResponse();
    }

    @GetMapping("/discussion-category")
    public CommonResult<List<Category>> getDiscussionCategory() {
        return CommonResult.successResponse(discussionService.getDiscussionCategory());
    }

    @PostMapping("/discussion-report")
    @RequiresAuthentication
    public CommonResult<Void> addDiscussionReport(@RequestBody DiscussionReport discussionReport) {
        discussionService.addDiscussionReport(discussionReport);
        return CommonResult.successResponse();
    }

}