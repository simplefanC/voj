package com.simplefanc.voj.backend.service.msg.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.discussion.CommentEntityService;
import com.simplefanc.voj.backend.dao.discussion.DiscussionEntityService;
import com.simplefanc.voj.backend.dao.discussion.ReplyEntityService;
import com.simplefanc.voj.backend.dao.msg.MsgRemindEntityService;
import com.simplefanc.voj.backend.dao.msg.UserSysNoticeEntityService;
import com.simplefanc.voj.backend.pojo.vo.UserMsgVO;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.pojo.vo.UserUnreadMsgCountVO;
import com.simplefanc.voj.backend.service.msg.UserMessageService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.discussion.Comment;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.discussion.Reply;
import com.simplefanc.voj.common.pojo.entity.msg.MsgRemind;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 10:36
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class UserMessageServiceImpl implements UserMessageService {

    private final MsgRemindEntityService msgRemindEntityService;

    private final ContestEntityService contestEntityService;

    private final ApplicationContext applicationContext;

    private final DiscussionEntityService discussionEntityService;

    private final CommentEntityService commentEntityService;

    private final ReplyEntityService replyEntityService;

    private final UserSysNoticeEntityService userSysNoticeEntityService;

    @Override
    public UserUnreadMsgCountVO getUnreadMsgCount() {
        // 获取当前登录的用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();
        UserUnreadMsgCountVO userUnreadMsgCount = msgRemindEntityService.getUserUnreadMsgCount(userRolesVO.getUid());
        if (userUnreadMsgCount == null) {
            userUnreadMsgCount = new UserUnreadMsgCountVO(0, 0, 0, 0, 0);
        }
        return userUnreadMsgCount;
    }

    @Override
    public void cleanMsg(String type, Long id) {
        // 获取当前登录的用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();
        boolean isOk = cleanMsgByType(type, id, userRolesVO.getUid());
        if (!isOk) {
            throw new StatusFailException("清空失败");
        }
    }

    @Override
    public IPage<UserMsgVO> getCommentMsg(Integer limit, Integer currentPage) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 5;
        }
        // 获取当前登录的用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        return getUserMsgList(userRolesVO.getUid(), "Discuss", limit, currentPage);
    }

    @Override
    public IPage<UserMsgVO> getReplyMsg(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 5;
        }

        // 获取当前登录的用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        return getUserMsgList(userRolesVO.getUid(), "Reply", limit, currentPage);
    }

    @Override
    public IPage<UserMsgVO> getLikeMsg(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 5;
        }

        // 获取当前登录的用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        return getUserMsgList(userRolesVO.getUid(), "Like", limit, currentPage);
    }

    private boolean cleanMsgByType(String type, Long id, String uid) {

        switch (type) {
            case "Like":
            case "Discuss":
            case "Reply":
                UpdateWrapper<MsgRemind> updateWrapper1 = new UpdateWrapper<>();
                updateWrapper1.eq(id != null, "id", id).eq("recipient_id", uid);
                return msgRemindEntityService.remove(updateWrapper1);
            case "Sys":
            case "Mine":
                UpdateWrapper<UserSysNotice> updateWrapper2 = new UpdateWrapper<>();
                updateWrapper2.eq(id != null, "id", id).eq("recipient_id", uid);
                return userSysNoticeEntityService.remove(updateWrapper2);
        }
        return false;
    }

    private IPage<UserMsgVO> getUserMsgList(String uid, String action, int limit, int currentPage) {
        Page<UserMsgVO> page = new Page<>(currentPage, limit);
        IPage<UserMsgVO> userMsgList = msgRemindEntityService.getUserMsg(page, uid, action);
        if (userMsgList.getTotal() > 0) {
            switch (action) {
                // 评论我的
                case "Discuss":
                    return getUserDiscussMsgList(userMsgList);
                // 回复我的
                case "Reply":
                    return getUserReplyMsgList(userMsgList);
                case "Like":
                    return getUserLikeMsgList(userMsgList);
                default:
                    throw new RuntimeException("invalid action:" + action);
            }
        } else {
            return userMsgList;
        }
    }

    private IPage<UserMsgVO> getUserDiscussMsgList(IPage<UserMsgVO> userMsgList) {

        List<Integer> discussionIds = userMsgList.getRecords().stream().map(UserMsgVO::getSourceId)
                .collect(Collectors.toList());
        Collection<Discussion> discussions = discussionEntityService.listByIds(discussionIds);
        for (Discussion discussion : discussions) {
            for (UserMsgVO userMsgVO : userMsgList.getRecords()) {
                if (Objects.equals(discussion.getId(), userMsgVO.getSourceId())) {
                    userMsgVO.setSourceTitle(discussion.getTitle());
                    break;
                }
            }
        }
        applicationContext.getBean(UserMessageService.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }

    private IPage<UserMsgVO> getUserReplyMsgList(IPage<UserMsgVO> userMsgList) {

        for (UserMsgVO userMsgVO : userMsgList.getRecords()) {
            if ("Discussion".equals(userMsgVO.getSourceType())) {
                Discussion discussion = discussionEntityService.getById(userMsgVO.getSourceId());
                if (discussion != null) {
                    userMsgVO.setSourceTitle(discussion.getTitle());
                } else {
                    userMsgVO.setSourceTitle("原讨论帖已被删除!【The original discussion post has been deleted!】");
                }
            } else if ("Contest".equals(userMsgVO.getSourceType())) {
                Contest contest = contestEntityService.getById(userMsgVO.getSourceId());
                if (contest != null) {
                    userMsgVO.setSourceTitle(contest.getTitle());
                } else {
                    userMsgVO.setSourceTitle("原比赛已被删除!【The original contest has been deleted!】");
                }
            }

            if ("Comment".equals(userMsgVO.getQuoteType())) {
                Comment comment = commentEntityService.getById(userMsgVO.getQuoteId());
                if (comment != null) {
                    String content;
                    if (comment.getContent().length() < 100) {
                        content = comment.getFromName() + " : " + comment.getContent();

                    } else {
                        content = comment.getFromName() + " : " + comment.getContent().substring(0, 100) + "...";
                    }
                    userMsgVO.setQuoteContent(content);
                } else {
                    userMsgVO.setQuoteContent("您的原评论信息已被删除！【Your original comments have been deleted!】");
                }

            } else if ("Reply".equals(userMsgVO.getQuoteType())) {
                Reply reply = replyEntityService.getById(userMsgVO.getQuoteId());
                if (reply != null) {
                    String content;
                    if (reply.getContent().length() < 100) {
                        content = reply.getFromName() + " : @" + reply.getToName() + "：" + reply.getContent();

                    } else {
                        content = reply.getFromName() + " : @" + reply.getToName() + "："
                                + reply.getContent().substring(0, 100) + "...";
                    }
                    userMsgVO.setQuoteContent(content);
                } else {
                    userMsgVO.setQuoteContent("您的原回复信息已被删除！【Your original reply has been deleted!】");
                }
            }

        }

        applicationContext.getBean(UserMessageService.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }

    private IPage<UserMsgVO> getUserLikeMsgList(IPage<UserMsgVO> userMsgList) {
        for (UserMsgVO userMsgVO : userMsgList.getRecords()) {
            if ("Discussion".equals(userMsgVO.getSourceType())) {
                Discussion discussion = discussionEntityService.getById(userMsgVO.getSourceId());
                if (discussion != null) {
                    userMsgVO.setSourceTitle(discussion.getTitle());
                } else {
                    userMsgVO.setSourceTitle("原讨论帖已被删除!【The original discussion post has been deleted!】");
                }
            } else if ("Contest".equals(userMsgVO.getSourceType())) {
                Contest contest = contestEntityService.getById(userMsgVO.getSourceId());
                if (contest != null) {
                    userMsgVO.setSourceTitle(contest.getTitle());
                } else {
                    userMsgVO.setSourceTitle("原比赛已被删除!【The original contest has been deleted!】");
                }
            }
        }
        applicationContext.getBean(UserMessageService.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }

    @Override
    @Async
    public void updateUserMsgRead(IPage<UserMsgVO> userMsgList) {
        List<Long> idList = userMsgList.getRecords().stream().filter(userMsgVO -> !userMsgVO.getState())
                .map(UserMsgVO::getId).collect(Collectors.toList());
        if (idList.size() == 0) {
            return;
        }
        UpdateWrapper<MsgRemind> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", idList).set("state", true);
        msgRemindEntityService.update(null, updateWrapper);
    }

}