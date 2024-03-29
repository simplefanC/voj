package com.simplefanc.voj.backend.pojo.dto;

import com.simplefanc.voj.common.pojo.entity.problem.CodeTemplate;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/5/30 15:09
 * @Description:
 */
@ToString
@Accessors(chain = true)
public class QDOJProblemDTO implements Serializable {

    private Problem problem;

    private List<String> languages;

    private List<ProblemCase> samples;

    private List<String> tags;

    private List<CodeTemplate> codeTemplates;

    private Boolean isSpj;

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<ProblemCase> getSamples() {
        return samples;
    }

    public void setSamples(List<ProblemCase> samples) {
        this.samples = samples;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<CodeTemplate> getCodeTemplates() {
        return codeTemplates;
    }

    public void setCodeTemplates(List<CodeTemplate> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }

    public Boolean getIsSpj() {
        return isSpj;
    }

    public void setIsSpj(Boolean spj) {
        isSpj = spj;
    }

}