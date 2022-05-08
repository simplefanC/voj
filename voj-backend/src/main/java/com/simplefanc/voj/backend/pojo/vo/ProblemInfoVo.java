package com.simplefanc.voj.backend.pojo.vo;

import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;

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