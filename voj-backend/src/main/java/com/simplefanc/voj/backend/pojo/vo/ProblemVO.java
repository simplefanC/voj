package com.simplefanc.voj.backend.pojo.vo;

import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/10/27 14:14
 * @Description:
 */
@ApiModel(value = "题目列表查询对象ProblemVO", description = "")
@Data
public class ProblemVO implements Serializable {

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "题目展示id")
    private String problemId;

    @ApiModelProperty(value = "题目标题")
    private String title;

    @ApiModelProperty(value = "题目难度")
    private Integer difficulty;

    @ApiModelProperty(value = "题目类型")
    private Integer type;

    @ApiModelProperty(value = "题目标签")
    private List<Tag> tags;

    // 以下为题目做题情况

    @ApiModelProperty(value = "该题总提交数")
    private Integer total = 0;

    @ApiModelProperty(value = "通过提交数")
    private Integer ac = 0;

    @ApiModelProperty(value = "空间超限提交数")
    private Integer mle = 0;

    @ApiModelProperty(value = "时间超限提交数")
    private Integer tle = 0;

    @ApiModelProperty(value = "运行错误提交数")
    private Integer re = 0;

    @ApiModelProperty(value = "格式错误提交数")
    private Integer pe = 0;

    @ApiModelProperty(value = "编译错误提交数")
    private Integer ce = 0;

    @ApiModelProperty(value = "答案错误提交数")
    private Integer wa = 0;

    @ApiModelProperty(value = "系统错误提交数")
    private Integer se = 0;

    @ApiModelProperty(value = "该IO题目分数总和")
    private Integer pa = 0;

    @ApiModelProperty(value = "IO题目总分数")
    private Integer score;

    public void setProblemCountVO(ProblemCountVO problemCountVO) {
        this.total = problemCountVO.getTotal() == null ? 0 : problemCountVO.getTotal();
        this.ac = problemCountVO.getAc() == null ? 0 : problemCountVO.getAc();
        this.mle = problemCountVO.getMle() == null ? 0 : problemCountVO.getMle();
        this.tle = problemCountVO.getTle() == null ? 0 : problemCountVO.getTle();
        this.re = problemCountVO.getRe() == null ? 0 : problemCountVO.getRe();
        this.pe = problemCountVO.getPe() == null ? 0 : problemCountVO.getPe();
        this.ce = problemCountVO.getCe() == null ? 0 : problemCountVO.getCe();
        this.wa = problemCountVO.getWa() == null ? 0 : problemCountVO.getWa();
        this.se = problemCountVO.getSe() == null ? 0 : problemCountVO.getSe();
        this.pa = problemCountVO.getPa() == null ? 0 : problemCountVO.getPa();
    }

}