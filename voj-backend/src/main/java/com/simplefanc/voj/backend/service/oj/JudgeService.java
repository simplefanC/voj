package com.simplefanc.voj.backend.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.dto.SubmitIdListDTO;
import com.simplefanc.voj.backend.pojo.dto.ToJudgeDTO;
import com.simplefanc.voj.backend.pojo.vo.JudgeVO;
import com.simplefanc.voj.backend.pojo.vo.SubmissionInfoVO;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeCase;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 11:12
 * @Description:
 */

public interface JudgeService {

    /**
     * @MethodName submitProblemJudge
     * @Description 核心方法 判题通过openfeign调用判题系统服务
     * @Since 2021/10/30
     */
    Judge submitProblemJudge(ToJudgeDTO judgeDTO);

    /**
     * @MethodName resubmit
     * @Description 调用判题服务器提交失败超过60s后，用户点击按钮重新提交判题进入的方法
     * @Since 2021/2/12
     */
    Judge resubmit(Long submitId);

    /**
     * @MethodName getSubmission
     * @Description 获取单个提交记录的详情
     * @Since 2021/1/2
     */
    SubmissionInfoVO getSubmission(Long submitId);

    /**
     * @MethodName updateSubmission
     * @Description 修改单个提交详情的分享权限
     * @Since 2021/1/2
     */
    void updateSubmission(Judge judge);

    /**
     * @MethodName getJudgeList
     * @Description 通用查询判题记录列表
     * @Since 2021/10/29
     */
    IPage<JudgeVO> getJudgeList(Integer limit, Integer currentPage, Boolean onlyMine, String searchPid,
                                Integer searchStatus, String searchUsername, Boolean completeProblemId);

    /**
     * @MethodName checkJudgeResult
     * @Description 对提交列表状态为Pending和Judging的提交进行更新检查
     * @Since 2021/1/3
     */
    HashMap<Long, Object> checkCommonJudgeResult(SubmitIdListDTO submitIdListDTO);

    /**
     * @MethodName checkContestJudgeResult
     * @Description 需要检查是否为封榜，是否可以查询结果，避免有人恶意查询
     * @Since 2021/6/11
     */
    HashMap<Long, Object> checkContestJudgeResult(SubmitIdListDTO submitIdListDTO);

    /**
     * @MethodName getJudgeCase
     * @Description 获得指定提交id的测试样例结果，暂不支持查看测试数据，只可看测试点结果，时间，空间，或者IO得分
     * @Since 2021/10/29
     */
    List<JudgeCase> getAllCaseResult(Long submitId);

}