package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.dao.discussion.CommentEntityService;
import com.simplefanc.voj.backend.dao.discussion.CommentLikeEntityService;
import com.simplefanc.voj.backend.dao.discussion.DiscussionEntityService;
import com.simplefanc.voj.backend.dao.discussion.ReplyEntityService;
import com.simplefanc.voj.backend.dao.user.UserAcproblemEntityService;
import com.simplefanc.voj.backend.pojo.dto.ReplyDto;
import com.simplefanc.voj.backend.pojo.vo.CommentListVo;
import com.simplefanc.voj.backend.pojo.vo.CommentVo;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.oj.CommentService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.discussion.Comment;
import com.simplefanc.voj.common.pojo.entity.discussion.CommentLike;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 15:59
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentEntityService commentEntityService;

    private final CommentLikeEntityService commentLikeEntityService;

    private final ReplyEntityService replyEntityService;

    private final DiscussionEntityService discussionEntityService;

    private final UserAcproblemEntityService userAcproblemEntityService;

    @Override
    public CommentListVo getComments(Long cid, Integer did, Integer limit, Integer currentPage) {

        // 如果有登录，则获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        boolean isRoot = UserSessionUtil.isRoot();

        IPage<CommentVo> commentList = commentEntityService.getCommentList(limit, currentPage, cid, did, isRoot,
                userRolesVo != null ? userRolesVo.getUid() : null);

        HashMap<Integer, Boolean> commentLikeMap = new HashMap<>();

        if (userRolesVo != null) {
            // 如果是有登录 需要检查是否对评论有点赞
            List<Integer> commentIdList = new LinkedList<>();

            for (CommentVo commentVo : commentList.getRecords()) {
                commentIdList.add(commentVo.getId());
            }

            if (commentIdList.size() > 0) {

                QueryWrapper<CommentLike> commentLikeQueryWrapper = new QueryWrapper<>();
                commentLikeQueryWrapper.in("cid", commentIdList);

                List<CommentLike> commentLikeList = commentLikeEntityService.list(commentLikeQueryWrapper);

                // 如果存在记录需要修正Map为true
                for (CommentLike tmp : commentLikeList) {
                    commentLikeMap.put(tmp.getCid(), true);
                }
            }
        }

        CommentListVo commentListVo = new CommentListVo();
        commentListVo.setCommentList(commentList);
        commentListVo.setCommentLikeMap(commentLikeMap);
        return commentListVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVo addComment(Comment comment) {

        if (StrUtil.isEmpty(comment.getContent().trim())) {
            throw new StatusFailException("评论内容不能为空！");
        }

        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        // 比赛外的评论 除管理员外 只有AC 10道以上才可评论
        if (comment.getCid() == null) {
            if (!UserSessionUtil.isRoot() && !UserSessionUtil.isAdmin()
                    && !UserSessionUtil.isProblemAdmin()) {
                QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uid", userRolesVo.getUid()).select("distinct pid");
                int userAcProblemCount = userAcproblemEntityService.count(queryWrapper);

                if (userAcProblemCount < 10) {
                    throw new StatusForbiddenException("对不起，您暂时不能评论！请先去提交题目通过10道以上!");
                }
            }
        }

        comment.setFromAvatar(userRolesVo.getAvatar())
                .setFromName(userRolesVo.getUsername())
                .setFromUid(userRolesVo.getUid());

        if (UserSessionUtil.isRoot()) {
            comment.setFromRole("root");
        } else if (UserSessionUtil.isAdmin() || UserSessionUtil.isProblemAdmin()) {
            comment.setFromRole("admin");
        } else {
            comment.setFromRole("user");
        }

        // 带有表情的字符串转换为编码
        comment.setContent(EmojiUtil.toHtml(comment.getContent()));

        boolean isOk = commentEntityService.saveOrUpdate(comment);

        if (isOk) {
            CommentVo commentVo = new CommentVo();
            commentVo.setContent(comment.getContent());
            commentVo.setId(comment.getId());
            commentVo.setFromAvatar(comment.getFromAvatar());
            commentVo.setFromName(comment.getFromName());
            commentVo.setFromUid(comment.getFromUid());
            commentVo.setLikeNum(0);
            commentVo.setGmtCreate(comment.getGmtCreate());
            commentVo.setReplyList(new LinkedList<>());
            // 如果是讨论区的回复，发布成功需要添加统计该讨论的回复数
            if (comment.getDid() != null) {
                Discussion discussion = discussionEntityService.getById(comment.getDid());
                if (discussion != null) {
                    discussion.setCommentNum(discussion.getCommentNum() + 1);
                    discussionEntityService.updateById(discussion);
                    // 更新消息
                    commentEntityService.updateCommentMsg(discussion.getUid(), userRolesVo.getUid(),
                            comment.getContent(), comment.getDid());
                }
            }
            return commentVo;
        } else {
            throw new StatusFailException("评论失败，请重新尝试！");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Comment comment) {
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        // 如果不是评论本人 或者不是管理员 无权限删除该评论
        if (comment.getFromUid().equals(userRolesVo.getUid()) || UserSessionUtil.isRoot()
                || UserSessionUtil.isAdmin() || UserSessionUtil.isProblemAdmin()) {

            // 获取需要删除该评论的回复数
            int replyNum = replyEntityService.count(new QueryWrapper<Reply>().eq("comment_id", comment.getId()));

            // 删除该数据 包括关联外键的reply表数据
            boolean isDeleteComment = commentEntityService.removeById(comment.getId());

            // 同时需要删除该评论的回复表数据
            replyEntityService.remove(new UpdateWrapper<Reply>().eq("comment_id", comment.getId()));

            if (isDeleteComment) {
                // 如果是讨论区的回复，删除成功需要减少统计该讨论的回复数
                if (comment.getDid() != null) {
                    UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
                    discussionUpdateWrapper.eq("id", comment.getDid())
                            .setSql("comment_num=comment_num-" + (replyNum + 1));
                    discussionEntityService.update(discussionUpdateWrapper);
                }
            } else {
                throw new StatusFailException("删除失败，请重新尝试");
            }

        } else {
            throw new StatusForbiddenException("无权删除该评论");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDiscussionLike(Integer cid, Boolean toLike, Integer sourceId, String sourceType) {

        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        QueryWrapper<CommentLike> commentLikeQueryWrapper = new QueryWrapper<>();
        commentLikeQueryWrapper.eq("cid", cid).eq("uid", userRolesVo.getUid());

        CommentLike commentLike = commentLikeEntityService.getOne(commentLikeQueryWrapper, false);
        // 添加点赞
        if (toLike) {
            // 如果不存在就添加
            if (commentLike == null) {
                boolean isSave = commentLikeEntityService
                        .saveOrUpdate(new CommentLike().setUid(userRolesVo.getUid()).setCid(cid));
                if (!isSave) {
                    throw new StatusFailException("点赞失败，请重试尝试！");
                }
            }
            // 点赞+1
            Comment comment = commentEntityService.getById(cid);
            if (comment != null) {
                comment.setLikeNum(comment.getLikeNum() + 1);
                commentEntityService.updateById(comment);
                commentEntityService.updateCommentLikeMsg(comment.getFromUid(), userRolesVo.getUid(), sourceId,
                        sourceType);
            }
        }
        // 取消点赞
        else {
            // 如果存在就删除
            if (commentLike != null) {
                boolean isDelete = commentLikeEntityService.removeById(commentLike.getId());
                if (!isDelete) {
                    throw new StatusFailException("取消点赞失败，请重试尝试！");
                }
            }
            // 点赞-1
            UpdateWrapper<Comment> commentUpdateWrapper = new UpdateWrapper<>();
            commentUpdateWrapper.setSql("like_num=like_num-1").eq("id", cid);
            commentEntityService.update(commentUpdateWrapper);
        }

    }

    @Override
    public List<Reply> getAllReply(Integer commentId, Long cid) {

        // 如果有登录，则获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        boolean isRoot = UserSessionUtil.isRoot();

        return commentEntityService.getAllReplyByCommentId(cid, userRolesVo != null ? userRolesVo.getUid() : null,
                isRoot, commentId);
    }

    @Override
    public void addReply(ReplyDto replyDto) {

        if (StrUtil.isEmpty(replyDto.getReply().getContent().trim())) {
            throw new StatusFailException("回复内容不能为空！");
        }

        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        Reply reply = replyDto.getReply();
        reply.setFromAvatar(userRolesVo.getAvatar())
                .setFromName(userRolesVo.getUsername())
                .setFromUid(userRolesVo.getUid());

        if (UserSessionUtil.isRoot()) {
            reply.setFromRole("root");
        } else if (UserSessionUtil.isAdmin() || UserSessionUtil.isProblemAdmin()) {
            reply.setFromRole("admin");
        } else {
            reply.setFromRole("user");
        }
        // 带有表情的字符串转换为编码
        reply.setContent(EmojiUtil.toHtml(reply.getContent()));

        boolean isOk = replyEntityService.saveOrUpdate(reply);

        if (isOk) {
            // 如果是讨论区的回复，发布成功需要增加统计该讨论的回复数
            if (replyDto.getDid() != null) {
                UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
                discussionUpdateWrapper.eq("id", replyDto.getDid()).setSql("comment_num=comment_num+1");
                discussionEntityService.update(discussionUpdateWrapper);
                // 更新消息
                replyEntityService.updateReplyMsg(replyDto.getDid(), "Discussion", reply.getContent(),
                        replyDto.getQuoteId(), replyDto.getQuoteType(), reply.getToUid(), reply.getFromUid());
            }
        } else {
            throw new StatusFailException("回复失败，请重新尝试！");
        }
    }

    @Override
    public void deleteReply(ReplyDto replyDto) {
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        Reply reply = replyDto.getReply();
        // 如果不是评论本人 或者不是管理员 无权限删除该评论
        if (reply.getFromUid().equals(userRolesVo.getUid()) || UserSessionUtil.isRoot()
                || UserSessionUtil.isAdmin() || UserSessionUtil.isProblemAdmin()) {
            // 删除该数据
            boolean isOk = replyEntityService.removeById(reply.getId());
            if (isOk) {
                // 如果是讨论区的回复，删除成功需要减少统计该讨论的回复数
                if (replyDto.getDid() != null) {
                    UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
                    discussionUpdateWrapper.eq("id", replyDto.getDid()).setSql("comment_num=comment_num-1");
                    discussionEntityService.update(discussionUpdateWrapper);
                }
            } else {
                throw new StatusFailException("删除失败，请重新尝试");
            }
        } else {
            throw new StatusForbiddenException("无权删除该回复");
        }
    }

}