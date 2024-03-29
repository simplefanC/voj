package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.DiscussionVO;
import com.simplefanc.voj.common.pojo.entity.discussion.Discussion;
import com.simplefanc.voj.common.pojo.entity.discussion.DiscussionReport;
import com.simplefanc.voj.common.pojo.entity.problem.Category;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 15:21
 * @Description:
 */

public interface DiscussionService {

    IPage<Discussion> getDiscussionList(Integer limit, Integer currentPage, Integer categoryId, String pid,
                                        Boolean onlyMine, String keyword, Boolean admin);

    DiscussionVO getDiscussion(Integer did);

    void addDiscussion(Discussion discussion);

    void updateDiscussion(Discussion discussion);

    void removeDiscussion(Integer did);

    void addDiscussionLike(Integer did, Boolean toLike);

    List<Category> getDiscussionCategory();

    void addDiscussionReport(DiscussionReport discussionReport);

}