package com.simplefanc.voj.backend.pojo.dto;

import com.simplefanc.voj.common.pojo.entity.problem.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/12/14 22:30
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ProblemDTO {

    private Problem problem;

    private List<ProblemCase> samples;

    private Boolean isUploadTestCase;

    private String uploadTestcaseDir;

    private String judgeMode;

    private Boolean changeModeCode;

    private List<Language> languages;

    private List<Tag> tags;

    private List<CodeTemplate> codeTemplates;

}