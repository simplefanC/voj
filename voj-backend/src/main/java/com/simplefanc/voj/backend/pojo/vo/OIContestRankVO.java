package com.simplefanc.voj.backend.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2021/1/18 18:16
 * @Description:
 */

@Data
@Accessors(chain = true)
public class OIContestRankVO {

    @ApiModelProperty(value = "序号")
    private Integer seq;

    @ApiModelProperty(value = "排名,排名为-1则为打星队伍")
    private Integer rank;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户真实姓名")
    private String realname;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "提交总得分")
    private Integer totalScore;

    @ApiModelProperty(value = "提交总耗时，只有满分的提交才会统计")
    private Integer totalTime;

    @ApiModelProperty(value = "OI的题对应提交得分")
    private Map<String, Integer> submissionInfo;

    @ApiModelProperty(value = "OI的题得满分后对应提交最优耗时")
    private Map<String, Integer> timeInfo;

    @ApiModelProperty(value = "是否已注册")
    private Boolean registered;

}