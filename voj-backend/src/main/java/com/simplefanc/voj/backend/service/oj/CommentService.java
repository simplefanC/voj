package com.simplefanc.voj.backend.service.oj;

import com.simplefanc.voj.backend.pojo.dto.ReplyDTO;
import com.simplefanc.voj.backend.pojo.vo.CommentListVO;
import com.simplefanc.voj.backend.pojo.vo.CommentVO;
import com.simplefanc.voj.common.pojo.entity.discussion.Comment;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 15:59
 * @Description:
 */

public interface CommentService {

    CommentListVO getComments(Long cid, Integer did, Integer limit, Integer currentPage);

    CommentVO addComment(Comment comment);

    void deleteComment(Comment comment);

    void addDiscussionLike(Integer cid, Boolean toLike, Integer sourceId, String sourceType);

    List<Reply> getAllReply(Integer commentId, Long cid);

    void addReply(ReplyDTO replyDTO);

    void deleteReply(ReplyDTO replyDTO);

}