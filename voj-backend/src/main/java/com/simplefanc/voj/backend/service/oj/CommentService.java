package com.simplefanc.voj.backend.service.oj;

import com.simplefanc.voj.backend.pojo.dto.ReplyDto;
import com.simplefanc.voj.backend.pojo.vo.CommentListVo;
import com.simplefanc.voj.backend.pojo.vo.CommentVo;
import com.simplefanc.voj.common.pojo.entity.discussion.Comment;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 15:59
 * @Description:
 */

public interface CommentService {

    CommentListVo getComments(Long cid, Integer did, Integer limit, Integer currentPage);

    CommentVo addComment(Comment comment);

    void deleteComment(Comment comment);

    void addDiscussionLike(Integer cid, Boolean toLike, Integer sourceId, String sourceType);

    List<Reply> getAllReply(Integer commentId, Long cid);

    void addReply(ReplyDto replyDto);

    void deleteReply(ReplyDto replyDto);

}