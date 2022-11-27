package com.simplefanc.voj.backend.service.admin.contest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.AdminContestVO;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:20
 * @Description:
 */

public interface AdminContestService {

    IPage<Contest> getContestList(Integer limit, Integer currentPage, String keyword);

    AdminContestVO getContest(Long cid);

    void deleteContest(Long cid);

    void addContest(AdminContestVO adminContestVO);

    void updateContest(AdminContestVO adminContestVO);

    void changeContestVisible(Long cid, String uid, Boolean visible);

    void cloneContest(Long cid);
}