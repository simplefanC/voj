package com.simplefanc.voj.service.admin.discussion.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.common.exception.StatusFailException;
import com.simplefanc.voj.dao.discussion.DiscussionEntityService;
import com.simplefanc.voj.dao.discussion.DiscussionReportEntityService;
import com.simplefanc.voj.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.pojo.entity.discussion.DiscussionReport;
import com.simplefanc.voj.service.admin.discussion.AdminDiscussionService;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 16:02
 * @Description:
 */
@Service
public class AdminDiscussionServiceImpl implements AdminDiscussionService {

    @Autowired
    private DiscussionEntityService discussionEntityService;

    @Autowired
    private DiscussionReportEntityService discussionReportEntityService;

    @Override
    public void updateDiscussion(Discussion discussion) {
        boolean isOk = discussionEntityService.updateById(discussion);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public void removeDiscussion(List<Integer> didList) {
        boolean isOk = discussionEntityService.removeByIds(didList);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }

    @Override
    public IPage<DiscussionReport> getDiscussionReport(Integer limit, Integer currentPage) {
        QueryWrapper<DiscussionReport> discussionReportQueryWrapper = new QueryWrapper<>();
        discussionReportQueryWrapper.orderByAsc("status");
        IPage<DiscussionReport> iPage = new Page<>(currentPage, limit);
        return discussionReportEntityService.page(iPage, discussionReportQueryWrapper);
    }

    @Override
    public void updateDiscussionReport(DiscussionReport discussionReport) {
        boolean isOk = discussionReportEntityService.updateById(discussionReport);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

}