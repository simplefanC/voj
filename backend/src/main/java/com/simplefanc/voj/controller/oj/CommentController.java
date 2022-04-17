package com.simplefanc.voj.controller.oj;

import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.pojo.dto.ReplyDto;
import com.simplefanc.voj.pojo.entity.discussion.Comment;
import com.simplefanc.voj.pojo.entity.discussion.Reply;
import com.simplefanc.voj.pojo.vo.CommentListVo;
import com.simplefanc.voj.pojo.vo.CommentVo;
import com.simplefanc.voj.service.oj.CommentService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/5/5 15:41
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;


    @GetMapping("/comments")
    public CommonResult<CommentListVo> getComments(@RequestParam(value = "cid", required = false) Long cid,
                                                   @RequestParam(value = "did", required = false) Integer did,
                                                   @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                                                   @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage) {
        return CommonResult.successResponse(commentService.getComments(cid, did, limit, currentPage));
    }


    @PostMapping("/comment")
    @RequiresPermissions("comment_add")
    @RequiresAuthentication
    public CommonResult<CommentVo> addComment(@RequestBody Comment comment) {
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
                                                @RequestParam("toLike") Boolean toLike,
                                                @RequestParam("sourceId") Integer sourceId,
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
    public CommonResult<Reply> addReply(@RequestBody ReplyDto replyDto) {
        commentService.addReply(replyDto);
        return CommonResult.successResponse();
    }

    @DeleteMapping("/reply")
    @RequiresAuthentication
    public CommonResult<Void> deleteReply(@RequestBody ReplyDto replyDto) {
        commentService.deleteReply(replyDto);
        return CommonResult.successResponse();
    }

}
