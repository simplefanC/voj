package com.simplefanc.voj.dao.discussion.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.dao.contest.ContestEntityService;
import com.simplefanc.voj.dao.discussion.CommentEntityService;
import com.simplefanc.voj.dao.discussion.ReplyEntityService;
import com.simplefanc.voj.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.dao.user.UserInfoEntityService;
import com.simplefanc.voj.mapper.CommentMapper;
import com.simplefanc.voj.pojo.entity.contest.Contest;
import com.simplefanc.voj.pojo.entity.discussion.Comment;
import com.simplefanc.voj.pojo.entity.discussion.Reply;
import com.simplefanc.voj.pojo.entity.msg.MsgRemind;
import com.simplefanc.voj.pojo.vo.CommentVo;
import com.simplefanc.voj.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Service
public class CommentEntityServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentEntityService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private ReplyEntityService replyEntityService;

    @Resource
    private MsgRemindEntityService msgRemindEntityService;

    @Override
    public IPage<CommentVo> getCommentList(int limit, int currentPage, Long cid, Integer did, Boolean isRoot, String uid) {
        //新建分页
        Page<CommentVo> page = new Page<>(currentPage, limit);

        if (cid != null) {
            Contest contest = contestEntityService.getById(cid);

            boolean onlyMineAndAdmin = contest.getStatus().equals(Constants.Contest.STATUS_RUNNING.getCode())
                    && !isRoot && !contest.getUid().equals(uid);
            if (onlyMineAndAdmin) { // 自己和比赛管理者评论可看
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
            boolean onlyMineAndAdmin = contest.getStatus().equals(Constants.Contest.STATUS_RUNNING.getCode())
                    && !isRoot && !contest.getUid().equals(uid);
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
        msgRemind.setAction("Discuss")
                .setRecipientId(recipientId)
                .setSenderId(senderId)
                .setSourceContent(content)
                .setSourceId(discussionId)
                .setSourceType("Discussion")
                .setUrl("/discussion-detail/" + discussionId);
        msgRemindEntityService.saveOrUpdate(msgRemind);
    }


    @Async
    @Override
    public void updateCommentLikeMsg(String recipientId, String senderId, Integer sourceId, String sourceType) {

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Like_Discuss")
                .setRecipientId(recipientId)
                .setSenderId(senderId)
                .setSourceId(sourceId)
                .setSourceType(sourceType)
                .setUrl(sourceType.equals("Discussion") ? "/discussion-detail/" + sourceId : "/contest/" + sourceId + "/comment");
        msgRemindEntityService.saveOrUpdate(msgRemind);
    }
}
