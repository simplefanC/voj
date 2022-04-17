package com.simplefanc.voj.service.oj.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.dao.problem.*;
import com.simplefanc.voj.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.pojo.entity.problem.*;
import com.simplefanc.voj.pojo.entity.training.TrainingCategory;
import com.simplefanc.voj.pojo.vo.CaptchaVo;
import com.simplefanc.voj.service.oj.CommonService;
import com.simplefanc.voj.utils.RedisUtils;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CommonServiceImpl implements CommonService {

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Autowired
    private RedisUtils redisUtil;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private CodeTemplateEntityService codeTemplateEntityService;

    @Autowired
    private TrainingCategoryEntityService trainingCategoryEntityService;


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
        List<Long> tidList = problemTagEntityService.listByMap(map)
                .stream()
                .map(ProblemTag::getTid)
                .collect(Collectors.toList());
        return tagEntityService.listByIds(tidList);
    }


    @Override
    public List<Language> getLanguages(Long pid, Boolean all) {
        // TODO 魔法值
        String oj = "ME";
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
        List<Long> idList = problemLanguageEntityService.list(queryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        return languageEntityService.listByIds(idList);

    }

    @Override
    public List<CodeTemplate> getProblemCodeTemplate(Long pid) {
        QueryWrapper<CodeTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        return codeTemplateEntityService.list(queryWrapper);
    }

}