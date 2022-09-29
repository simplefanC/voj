package com.simplefanc.voj.backend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @Author chenfan
 * @Date 2022/9/20
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SwitchConfigDto {

    /**
     * 是否开启公开评论区
     */
//    private Boolean openPublicDiscussion;

    /**
     * 是否开启比赛讨论区
     */
//    private Boolean openContestComment;

    /**
     * 是否开启公开评测
     */
    private Boolean openPublicJudge;

    /**
     * 是否开启比赛评测
     */
    private Boolean openContestJudge;

    /**
     * 非比赛的提交间隔秒数
     */
    private Integer defaultSubmitInterval;

    private Long codeVisibleStartTime;

    /**
     * 是否允许注册
     */
    private Boolean register;

    private Boolean problem;

    private Boolean training;

    private Boolean contest;

    private Boolean status;

    private Boolean rank;

    private Boolean discussion;

    private Boolean introduction;

}
