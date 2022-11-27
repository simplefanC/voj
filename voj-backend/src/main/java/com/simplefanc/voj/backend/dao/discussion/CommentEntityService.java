package com.simplefanc.voj.backend.dao.discussion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.CommentVO;
import com.simplefanc.voj.common.pojo.entity.discussion.Comment;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
public interface CommentEntityService extends IService<Comment> {

    IPage<CommentVO> getCommentList(int limit, int currentPage, Long cid, Integer did, Boolean isRoot, String uid);

    List<Reply> getAllReplyByCommentId(Long cid, String uid, Boolean isRoot, Integer commentId);

    void updateCommentMsg(String recipientId, String senderId, String content, Integer discussionId);

    void updateCommentLikeMsg(String recipientId, String senderId, Integer sourceId, String sourceType);

}
