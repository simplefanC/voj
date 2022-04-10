package com.simplefanc.voj.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.simplefanc.voj.pojo.entity.problem.Problem;
import com.simplefanc.voj.pojo.entity.problem.Tag;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2020/12/30 21:37
 * @Description:
 */
@Data
@AllArgsConstructor
public class ProblemInfoVo {
    private Problem problem;
    private List<Tag> tags;
    private List<String> languages;
    private ProblemCountVo problemCount;
    private HashMap<String, String> codeTemplate;
}