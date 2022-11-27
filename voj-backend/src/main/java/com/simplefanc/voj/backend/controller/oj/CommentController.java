package com.simplefanc.voj.backend.controller.oj;

import com.simplefanc.voj.backend.pojo.dto.ReplyDTO;
import com.simplefanc.voj.backend.pojo.vo.CommentListVO;
import com.simplefanc.voj.backend.pojo.vo.CommentVO;
import com.simplefanc.voj.backend.service.oj.CommentService;
import com.simplefanc.voj.common.pojo.entity.discussion.Comment;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/5/5 15:41
 * @Description:
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public CommonResult<CommentListVO> getComments(@RequestParam(value = "cid", required = false) Long cid,
                                                   @RequestParam(value = "did", required = false) Integer did,
                                                   @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                                                   @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage) {
        return CommonResult.successResponse(commentService.getComments(cid, did, limit, currentPage));
    }

    @PostMapping("/comment")
    @RequiresPermissions("comment_add")
    @RequiresAuthentication
    public CommonResult<CommentVO> addComment(@RequestBody Comment comment) {
        return CommonResult.successResponse(commentService.addComment(comment));
    }

    @DeleteMapping("/comment")
    @RequiresAuthentication
    public CommonResult<Void> deleteComment(@RequestBody Comment comment) {
        commentService.deleteComment(comment);
        return CommonResult.successResponse();
    }

    @GetMapping("/comment-like")
    @RequiresAuthentication
    public CommonResult<Void> addDiscussionLike(@RequestParam("cid") Integer cid,
                                                @RequestParam("toLike") Boolean toLike, @RequestParam("sourceId") Integer sourceId,
                                                @RequestParam("sourceType") String sourceType) {
        commentService.addDiscussionLike(cid, toLike, sourceId, sourceType);
        return CommonResult.successResponse();
    }

    @GetMapping("/reply")
    public CommonResult<List<Reply>> getAllReply(@RequestParam("commentId") Integer commentId,
                                                 @RequestParam(value = "cid", required = false) Long cid) {
        return CommonResult.successResponse(commentService.getAllReply(commentId, cid));
    }

    @PostMapping("/reply")
    @RequiresPermissions("reply_add")
    @RequiresAuthentication
    public CommonResult<Reply> addReply(@RequestBody ReplyDTO replyDTO) {
        commentService.addReply(replyDTO);
        return CommonResult.successResponse();
    }

    @DeleteMapping("/reply")
    @RequiresAuthentication
    public CommonResult<Void> deleteReply(@RequestBody ReplyDTO replyDTO) {
        commentService.deleteReply(replyDTO);
        return CommonResult.successResponse();
    }

}
