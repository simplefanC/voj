package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.problem.*;
import com.simplefanc.voj.backend.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.backend.pojo.vo.CaptchaVo;
import com.simplefanc.voj.backend.service.oj.CommonService;
import com.simplefanc.voj.common.constants.Constant;
import com.simplefanc.voj.common.pojo.entity.problem.*;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:28
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final TagEntityService tagEntityService;

    private final ProblemTagEntityService problemTagEntityService;

    private final LanguageEntityService languageEntityService;

    private final ProblemLanguageEntityService problemLanguageEntityService;

    private final RedisUtil redisUtil;

    private final ProblemEntityService problemEntityService;

    private final CodeTemplateEntityService codeTemplateEntityService;

    private final TrainingCategoryEntityService trainingCategoryEntityService;

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(90, 30, 4);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.simpleUUID();
        // 存入redis并设置过期时间为30分钟
        redisUtil.set(key, verCode, 1800);
        // 将key和base64返回给前端
        CaptchaVo captchaVo = new CaptchaVo();
        captchaVo.setImg(specCaptcha.toBase64());
        captchaVo.setCaptchaKey(key);
        return captchaVo;
    }

    @Override
    public List<TrainingCategory> getTrainingCategory() {
        return trainingCategoryEntityService.list();
    }

    @Override
    public List<Tag> getAllProblemTagsList(String oj) {
        List<Tag> tagList;
        oj = oj.toUpperCase();
        if (oj.equals("ALL")) {
            tagList = tagEntityService.list();
        } else {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("oj", oj);
            tagList = tagEntityService.list(tagQueryWrapper);
        }
        return tagList;
    }

    @Override
    public Collection<Tag> getProblemTags(Long pid) {
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        List<Long> tidList = problemTagEntityService.listByMap(map).stream().map(ProblemTag::getTid)
                .collect(Collectors.toList());
        return tagEntityService.listByIds(tidList);
    }

    @Override
    public List<Language> getLanguages(Long pid, Boolean all) {
        String oj = Constant.LOCAL;
        if (pid != null) {
            Problem problem = problemEntityService.getById(pid);
            if (problem.getIsRemote()) {
                oj = problem.getProblemId().split("-")[0];
            }
        }

        QueryWrapper<Language> queryWrapper = new QueryWrapper<>();
        // 获取对应OJ支持的语言列表
        queryWrapper.eq(all != null && !all, "oj", oj);
        return languageEntityService.list(queryWrapper);
    }

    @Override
    public Collection<Language> getProblemLanguages(Long pid) {
        QueryWrapper<ProblemLanguage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid).select("lid");
        List<Long> idList = problemLanguageEntityService.list(queryWrapper).stream().map(ProblemLanguage::getLid)
                .collect(Collectors.toList());
        return languageEntityService.listByIds(idList);

    }

    @Override
    public List<CodeTemplate> getProblemCodeTemplate(Long pid) {
        QueryWrapper<CodeTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        return codeTemplateEntityService.list(queryWrapper);
    }

}