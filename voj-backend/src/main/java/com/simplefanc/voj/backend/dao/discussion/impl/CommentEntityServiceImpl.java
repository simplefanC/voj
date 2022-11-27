package com.simplefanc.voj.backend.dao.discussion.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.discussion.CommentEntityService;
import com.simplefanc.voj.backend.dao.discussion.ReplyEntityService;
import com.simplefanc.voj.backend.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.mapper.CommentMapper;
import com.simplefanc.voj.backend.pojo.vo.CommentVO;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.discussion.Comment;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Service
@RequiredArgsConstructor
public class CommentEntityServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentEntityService {

    private final CommentMapper commentMapper;

    private final ContestEntityService contestEntityService;

    private final UserInfoEntityService userInfoEntityService;

    private final ReplyEntityService replyEntityService;

    private final MsgRemindEntityService msgRemindEntityService;

    @Override
    public IPage<CommentVO> getCommentList(int limit, int currentPage, Long cid, Integer did, Boolean isRoot,
                                           String uid) {
        // 新建分页
        Page<CommentVO> page = new Page<>(currentPage, limit);

        if (cid != null) {
            Contest contest = contestEntityService.getById(cid);

            boolean onlyMineAndAdmin = contest.getStatus().equals(ContestEnum.STATUS_RUNNING.getCode()) && !isRoot
                    && !contest.getUid().equals(uid);
            // 自己和比赛管理者评论可看
            if (onlyMineAndAdmin) {
                List<String> myAndAdminUidList = userInfoEntityService.getSuperAdminUidList();
                myAndAdminUidList.add(uid);
                myAndAdminUidList.add(contest.getUid());
                return commentMapper.getCommentList(page, cid, did, true, myAndAdminUidList);
            }

        }
        return commentMapper.getCommentList(page, cid, did, false, null);
    }

    @Override
    public List<Reply> getAllReplyByCommentId(Long cid, String uid, Boolean isRoot, Integer commentId) {
        QueryWrapper<Reply> replyQueryWrapper = new QueryWrapper<>();
        replyQueryWrapper.eq("comment_id", commentId);

        if (cid != null) {
            Contest contest = contestEntityService.getById(cid);
            boolean onlyMineAndAdmin = contest.getStatus().equals(ContestEnum.STATUS_RUNNING.getCode()) && !isRoot
                    && !contest.getUid().equals(uid);
            // 自己和比赛管理者评论可看
            if (onlyMineAndAdmin) {
                List<String> myAndAdminUidList = userInfoEntityService.getSuperAdminUidList();
                myAndAdminUidList.add(uid);
                myAndAdminUidList.add(contest.getUid());
                replyQueryWrapper.in("from_uid", myAndAdminUidList);
            }

        }
        replyQueryWrapper.orderByDesc("gmt_create");
        return replyEntityService.list(replyQueryWrapper);
    }

    @Async
    @Override
    public void updateCommentMsg(String recipientId, String senderId, String content, Integer discussionId) {

        if (content.length() > 200) {
            content = content.substring(0, 200) + "...";
        }

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Discuss").setRecipientId(recipientId).setSenderId(senderId).setSourceContent(content)
                .setSourceId(discussionId).setSourceType("Discussion").setUrl("/discussion-detail/" + discussionId);
        msgRemindEntityService.saveOrUpdate(msgRemind);
    }

    @Async
    @Override
    public void updateCommentLikeMsg(String recipientId, String senderId, Integer sourceId, String sourceType) {

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Like_Discuss").setRecipientId(recipientId).setSenderId(senderId).setSourceId(sourceId)
                .setSourceType(sourceType).setUrl("Discussion".equals(sourceType) ? "/discussion-detail/" + sourceId
                : "/contest/" + sourceId + "/comment");
        msgRemindEntityService.saveOrUpdate(msgRemind);
    }

}
