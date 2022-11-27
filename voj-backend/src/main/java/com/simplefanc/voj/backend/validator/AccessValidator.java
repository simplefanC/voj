package com.simplefanc.voj.backend.validator;

import com.simplefanc.voj.backend.common.constants.AccessEnum;
import com.simplefanc.voj.backend.common.exception.StatusAccessDeniedException;
import com.simplefanc.voj.backend.config.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author chenfan
 * @Date 2022/9/25
 */
@Component
@RequiredArgsConstructor
public class AccessValidator {

    private final ConfigVO configVO;

    public void validateAccess(AccessEnum accessEnum) throws StatusAccessDeniedException {
        switch (accessEnum) {
//            case PUBLIC_DISCUSSION:
//                if (!configVO.getOpenPublicDiscussion()) {
//                    throw new StatusAccessDeniedException("网站当前未开启公开讨论区的功能，不可访问！");
//                }
//                break;
//            case CONTEST_COMMENT:
//                if (!configVO.getOpenContestComment()) {
//                    throw new StatusAccessDeniedException("网站当前未开启比赛评论区的功能，不可访问！");
//                }
//                break;
            case PUBLIC_JUDGE:
                if (!configVO.getOpenPublicJudge()) {
                    throw new StatusAccessDeniedException("网站当前未开启题目评测的功能，禁止提交或调试！");
                }
                break;
            case CONTEST_JUDGE:
                if (!configVO.getOpenContestJudge()) {
                    throw new StatusAccessDeniedException("网站当前未开启比赛题目评测的功能，禁止提交或调试！");
                }
                break;
            default:
        }
    }
}
