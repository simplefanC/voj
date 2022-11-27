package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.problem.*;
import com.simplefanc.voj.backend.dao.training.TrainingCategoryEntityService;
import com.simplefanc.voj.backend.pojo.vo.CaptchaVO;
import com.simplefanc.voj.backend.pojo.vo.ProblemTagVO;
import com.simplefanc.voj.backend.service.oj.CommonService;
import com.simplefanc.voj.common.constants.Constant;
import com.simplefanc.voj.common.pojo.entity.problem.*;
import com.simplefanc.voj.common.pojo.entity.training.TrainingCategory;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
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

    private final TagClassificationEntityService tagClassificationEntityService;

    private final ProblemTagEntityService problemTagEntityService;

    private final LanguageEntityService languageEntityService;

    private final ProblemLanguageEntityService problemLanguageEntityService;

    private final RedisUtil redisUtil;

    private final ProblemEntityService problemEntityService;

    private final CodeTemplateEntityService codeTemplateEntityService;

    private final TrainingCategoryEntityService trainingCategoryEntityService;

    @Override
    public CaptchaVO getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(90, 30, 4);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.simpleUUID();
        // 存入redis并设置过期时间为30分钟
        redisUtil.set(key, verCode, 1800);
        // 将key和base64返回给前端
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setImg(specCaptcha.toBase64());
        captchaVO.setCaptchaKey(key);
        return captchaVO;
    }

    @Override
    public List<TrainingCategory> getTrainingCategory() {
        return trainingCategoryEntityService.list();
    }

    @Override
    public List<Tag> getAllProblemTagsList(String oj) {
        List<Tag> tagList;
        oj = oj.toUpperCase();
        if ("ALL".equals(oj)) {
            tagList = tagEntityService.list();
        } else {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("oj", oj);
            tagList = tagEntityService.list(tagQueryWrapper);
        }
        return tagList;
    }

    @Override
    public List<ProblemTagVO> getProblemTagsAndClassification(String oj) {
        oj = oj.toUpperCase();
        List<ProblemTagVO> problemTagVOList = new ArrayList<>();
        List<TagClassification> classificationList = null;
        List<Tag> tagList = null;
        if ("ALL".equals(oj)) {
            classificationList = tagClassificationEntityService.list();
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagList = tagEntityService.list(tagQueryWrapper);
        } else {
            QueryWrapper<TagClassification> tagClassificationQueryWrapper = new QueryWrapper<>();
            tagClassificationQueryWrapper.eq("oj", oj)
                    .orderByAsc("`rank`");
            classificationList = tagClassificationEntityService.list(tagClassificationQueryWrapper);

            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("oj", oj);
            tagList = tagEntityService.list(tagQueryWrapper);
        }
        if (CollectionUtils.isEmpty(classificationList)) {
            ProblemTagVO problemTagVO = new ProblemTagVO();
            problemTagVO.setTagList(tagList);
            problemTagVOList.add(problemTagVO);
        } else {
            for (TagClassification classification : classificationList) {
                ProblemTagVO problemTagVO = new ProblemTagVO();
                problemTagVO.setClassification(classification);
                List<Tag> tags = new ArrayList<>();
                if (!CollectionUtils.isEmpty(tagList)) {
                    Iterator<Tag> it = tagList.iterator();
                    while (it.hasNext()) {
                        Tag tag = it.next();
                        if (classification.getId().equals(tag.getTcid())) {
                            tags.add(tag);
                            it.remove();
                        }
                    }
                }
                problemTagVO.setTagList(tags);
                problemTagVOList.add(problemTagVO);
            }
            if (tagList.size() > 0) {
                ProblemTagVO problemTagVO = new ProblemTagVO();
                problemTagVO.setTagList(tagList);
                problemTagVOList.add(problemTagVO);
            }
        }

        if ("ALL".equals(oj)) {
            Collections.sort(problemTagVOList, problemTagVOComparator);
        }
        return problemTagVOList;
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

    private Comparator<ProblemTagVO> problemTagVOComparator = (p1, p2) -> {
        if (p1 == null) {
            return 1;
        }
        if (p2 == null) {
            return 1;
        }
        if (p1.getClassification() == null) {
            return 1;
        }
        if (p2.getClassification() == null) {
            return -1;
        }
        TagClassification p1Classification = p1.getClassification();
        TagClassification p2Classification = p2.getClassification();
        if (Objects.equals(p1Classification.getOj(), p2Classification.getOj())) {
            return p1Classification.getRank().compareTo(p2Classification.getRank());
        } else {
            if (Constant.LOCAL.equals(p1Classification.getOj())) {
                return -1;
            } else if (Constant.LOCAL.equals(p2Classification.getOj())) {
                return 1;
            } else {
                return p1Classification.getOj().compareTo(p2Classification.getOj());
            }
        }
    };

}