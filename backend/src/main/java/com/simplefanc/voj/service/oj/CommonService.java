package com.simplefanc.voj.service.oj;

import com.simplefanc.voj.pojo.entity.problem.CodeTemplate;
import com.simplefanc.voj.pojo.entity.problem.Language;
import com.simplefanc.voj.pojo.entity.problem.Tag;
import com.simplefanc.voj.pojo.entity.training.TrainingCategory;
import com.simplefanc.voj.pojo.vo.CaptchaVo;

import java.util.Collection;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:28
 * @Description:
 */

public interface CommonService {

    CaptchaVo getCaptcha();

    List<TrainingCategory> getTrainingCategory();

    List<Tag> getAllProblemTagsList(String oj);

    Collection<Tag> getProblemTags(Long pid);

    List<Language> getLanguages(Long pid, Boolean all);

    Collection<Language> getProblemLanguages(Long pid);

    List<CodeTemplate> getProblemCodeTemplate(Long pid);
}