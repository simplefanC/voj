package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.ContestPrintDTO;
import com.simplefanc.voj.backend.pojo.dto.ContestRankDTO;
import com.simplefanc.voj.backend.pojo.dto.RegisterContestDTO;
import com.simplefanc.voj.backend.pojo.dto.UserReadContestAnnouncementDTO;
import com.simplefanc.voj.backend.pojo.vo.*;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;

import java.util.List;
import java.util.Set;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 22:26
 * @Description:
 */
public interface ContestService {

    IPage<ContestVO> getContestList(Integer limit, Integer currentPage, Integer status, Integer type, String keyword);

    ContestVO getContestInfo(Long cid);

    void toRegisterContest(RegisterContestDTO registerContestDTO);

    AccessVO getContestAccess(Long cid);

    List<ContestProblemVO> getContestProblem(Long cid);

    ProblemInfoVO getContestProblemDetails(Long cid, String displayId);

    // TODO 参数过多
    IPage<JudgeVO> getContestSubmissionList(Integer limit, Integer currentPage, Boolean onlyMine, String displayId,
                                            Integer searchStatus, String searchUsername, Long searchCid, Boolean beforeContestSubmit,
                                            Boolean completeProblemId);

    IPage getContestRank(ContestRankDTO contestRankDTO);

    Set<String> getContestAdminUidList(Contest contest);

    IPage<AnnouncementVO> getContestAnnouncement(Long cid, Integer limit, Integer currentPage);

    List<Announcement> getContestUserNotReadAnnouncement(UserReadContestAnnouncementDTO userReadContestAnnouncementDTO);

    void submitPrintText(ContestPrintDTO contestPrintDTO);

}