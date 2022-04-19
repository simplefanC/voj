package com.simplefanc.voj.server.service.admin.tag;


import com.simplefanc.voj.common.pojo.entity.problem.Tag;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 17:47
 * @Description:
 */

public interface AdminTagService {
    Tag addProblem(Tag tag);

    void updateTag(Tag tag);

    void deleteTag(Long tid);
}