package com.simplefanc.voj.backend.service.admin.discussion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.discussion.DiscussionReport;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 16:02
 * @Description:
 */

public interface AdminDiscussionService {
    void updateDiscussion(Discussion discussion);

    void removeDiscussion(List<Integer> didList);

    IPage<DiscussionReport> getDiscussionReport(Integer limit, Integer currentPage);

    void updateDiscussionReport(DiscussionReport discussionReport);

}