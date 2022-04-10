package com.simplefanc.voj.service.oj.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.simplefanc.voj.common.exception.StatusFailException;
import com.simplefanc.voj.common.exception.StatusForbiddenException;
import com.simplefanc.voj.common.exception.StatusNotFoundException;
import com.simplefanc.voj.dao.discussion.DiscussionEntityService;
import com.simplefanc.voj.dao.discussion.DiscussionLikeEntityService;
import com.simplefanc.voj.dao.discussion.DiscussionReportEntityService;
import com.simplefanc.voj.dao.problem.CategoryEntityService;
import com.simplefanc.voj.dao.user.UserAcproblemEntityService;
import com.simplefanc.voj.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.pojo.entity.discussion.DiscussionLike;
import com.simplefanc.voj.pojo.entity.discussion.DiscussionReport;
import com.simplefanc.voj.pojo.entity.problem.Category;
import com.simplefanc.voj.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.pojo.vo.DiscussionVo;
import com.simplefanc.voj.pojo.vo.UserRolesVo;
import com.simplefanc.voj.service.oj.DiscussionService;
import com.simplefanc.voj.utils.Constants;
import com.simplefanc.voj.utils.RedisUtils;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 15:21
 * @Description:
 */
@Service
public class DiscussionServiceImpl implements DiscussionService {
    @Autowired
    private DiscussionEntityService discussionEntityService;

    @Autowired
    private DiscussionLikeEntityService discussionLikeEntityService;

    @Autowired
    private CategoryEntityService categoryEntityService;

    @Autowired
    private DiscussionReportEntityService discussionReportEntityService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Override
    public IPage<Discussion> getDiscussionList(Integer limit,
                                               Integer currentPage,
                                               Integer categoryId,
                                               String pid,
                                               Boolean onlyMine,
                                               String keyword,
                                               Boolean admin) {

        QueryWrapper<Discussion> discussionQueryWrapper = new QueryWrapper<>();

        IPage<Discussion> iPage = new Page<>(currentPage, limit);

        if (categoryId != null) {
            discussionQueryWrapper.eq("category_id", categoryId);
        }

        if (!StringUtils.isEmpty(keyword)) {

            final String key = keyword.trim();

            discussionQueryWrapper.and(wrapper -> wrapper.like("title", key).or()
                    .like("author", key).or()
                    .like("id", key).or()
                    .like("description", key));
        }

        if (!StringUtils.isEmpty(pid)) {
            discussionQueryWrapper.eq("pid", pid);
        }


        boolean isAdmin = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("problem_admin")
                || SecurityUtils.getSubject().hasRole("admin");
        discussionQueryWrapper
                .eq(!(admin && isAdmin), "status", 0)
                .orderByDesc("top_priority")
                .orderByDesc("gmt_create")
                .orderByDesc("like_num")
                .orderByDesc("view_num");

        if (onlyMine) {
            Session session = SecurityUtils.getSubject().getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
            discussionQueryWrapper.eq("uid", userRolesVo.getUid());
        }

        return discussionEntityService.page(iPage, discussionQueryWrapper);
    }

    @Override
    public DiscussionVo getDiscussion(Integer did) {

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        String uid = null;

        if (userRolesVo != null) {
            uid = userRolesVo.getUid();
        }

        DiscussionVo discussion = discussionEntityService.getDiscussion(did, uid);

        if (discussion == null) {
            throw new StatusNotFoundException("对不起，该讨论不存在！");
        }

        if (discussion.getStatus() == 1) {
            throw new StatusForbiddenException("对不起，该讨论已被封禁！");
        }

        // 浏览量+1
        UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
        discussionUpdateWrapper.setSql("view_num=view_num+1").eq("id", discussion.getId());
        discussionEntityService.update(discussionUpdateWrapper);
        discussion.setViewNum(discussion.getViewNum() + 1);

        return discussion;
    }

    @Override
    public void addDiscussion(Discussion discussion) {

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 除管理员外 其它用户需要AC20道题目以上才可发帖，同时限制一天只能发帖5次
        if (!SecurityUtils.getSubject().hasRole("root")
                && !SecurityUtils.getSubject().hasRole("admin")
                && !SecurityUtils.getSubject().hasRole("problem_admin")) {

            QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", userRolesVo.getUid()).select("distinct pid");
            int userAcProblemCount = userAcproblemEntityService.count(queryWrapper);

            if (userAcProblemCount < 20) {
                throw new StatusForbiddenException("对不起，您暂时无权限发帖！请先去提交题目通过20道以上!");
            }

            String lockKey = Constants.Account.DISCUSSION_ADD_NUM_LOCK.getCode() + userRolesVo.getUid();
            Integer num = (Integer) redisUtils.get(lockKey);
            if (num == null) {
                redisUtils.set(lockKey, 1, 3600 * 24);
            } else if (num >= 5) {
                throw new StatusForbiddenException("对不起，您今天发帖次数已超过5次，已被限制！");
            } else {
                redisUtils.incr(lockKey, 1);
            }
        }

        discussion.setAuthor(userRolesVo.getUsername())
                .setAvatar(userRolesVo.getAvatar())
                .setUid(userRolesVo.getUid());

        if (SecurityUtils.getSubject().hasRole("root")) {
            discussion.setRole("root");
        } else if (SecurityUtils.getSubject().hasRole("admin")
                || SecurityUtils.getSubject().hasRole("problem_admin")) {
            discussion.setRole("admin");
        } else {
            // 如果不是管理员角色，一律重置为不置顶
            discussion.setTopPriority(false);
        }

        boolean isOk = discussionEntityService.saveOrUpdate(discussion);
        if (!isOk) {
            throw new StatusFailException("发布失败，请重新尝试！");
        }
    }


    @Override
    public void updateDiscussion(Discussion discussion) {
        boolean isOk = discussionEntityService.updateById(discussion);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public void removeDiscussion(Integer did) {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<Discussion>().eq("id", did);
        // 如果不是是管理员,则需要附加当前用户的uid条件
        if (!SecurityUtils.getSubject().hasRole("root")
                && !SecurityUtils.getSubject().hasRole("admin")
                && !SecurityUtils.getSubject().hasRole("problem_admin")) {
            discussionUpdateWrapper.eq("uid", userRolesVo.getUid());
        }
        boolean isOk = discussionEntityService.remove(discussionUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("删除失败，无权限或者该讨论不存在");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDiscussionLike(Integer did, Boolean toLike) {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<DiscussionLike> discussionLikeQueryWrapper = new QueryWrapper<>();
        discussionLikeQueryWrapper.eq("did", did).eq("uid", userRolesVo.getUid());

        DiscussionLike discussionLike = discussionLikeEntityService.getOne(discussionLikeQueryWrapper, false);

        if (toLike) { // 添加点赞
            if (discussionLike == null) { // 如果不存在就添加
                boolean isSave = discussionLikeEntityService.saveOrUpdate(new DiscussionLike().setUid(userRolesVo.getUid()).setDid(did));
                if (!isSave) {
                    throw new StatusFailException("点赞失败，请重试尝试！");
                }
            }
            // 点赞+1
            Discussion discussion = discussionEntityService.getById(did);
            if (discussion != null) {
                discussion.setLikeNum(discussion.getLikeNum() + 1);
                discussionEntityService.updateById(discussion);
                // 更新点赞消息
                discussionEntityService.updatePostLikeMsg(discussion.getUid(), userRolesVo.getUid(), did);
            }
        } else { // 取消点赞
            if (discussionLike != null) { // 如果存在就删除
                boolean isDelete = discussionLikeEntityService.removeById(discussionLike.getId());
                if (!isDelete) {
                    throw new StatusFailException("取消点赞失败，请重试尝试！");
                }
            }
            // 点赞-1
            UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
            discussionUpdateWrapper.setSql("like_num=like_num-1").eq("id", did);
            discussionEntityService.update(discussionUpdateWrapper);
        }

    }

    @Override
    public List<Category> getDiscussionCategory() {
        return categoryEntityService.list();
    }

    @Override
    public void addDiscussionReport(DiscussionReport discussionReport) {
        boolean isOk = discussionReportEntityService.saveOrUpdate(discussionReport);
        if (!isOk) {
            throw new StatusFailException("举报失败，请重新尝试");
        }
    }
}