package com.simplefanc.voj.backend.service.oj;

import com.simplefanc.voj.backend.pojo.vo.CaptchaVO;
import com.simplefanc.voj.backend.pojo.vo.ProblemTagVO;
import com.simplefanc.voj.common.pojo.entity.problem.CodeTemplate;
import com.simplefanc.voj.common.pojo.entity.problem.Language;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;

import java.util.Collection;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:28
 * @Description:
 */

public interface CommonService {

    CaptchaVO getCaptcha();

    List<TrainingCategory> getTrainingCategory();

    List<Tag> getAllProblemTagsList(String oj);

    List<ProblemTagVO> getProblemTagsAndClassification(String oj);

    Collection<Tag> getProblemTags(Long pid);

    List<Language> getLanguages(Long pid, Boolean all);

    Collection<Language> getProblemLanguages(Long pid);

    List<CodeTemplate> getProblemCodeTemplate(Long pid);

}