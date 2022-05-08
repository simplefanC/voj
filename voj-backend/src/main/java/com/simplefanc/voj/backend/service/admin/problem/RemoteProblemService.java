package com.simplefanc.voj.backend.service.admin.problem;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.dao.problem.*;
import com.simplefanc.voj.backend.judge.remote.crawler.CrawlersHolder;
import com.simplefanc.voj.backend.judge.remote.crawler.ProblemCrawler;
import com.simplefanc.voj.common.pojo.entity.problem.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 17:33
 * @Description:
 */
@Component
public class RemoteProblemService {

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    public ProblemCrawler.RemoteProblemInfo getOtherOJProblemInfo(String ojName, String problemId, String author)
            throws Exception {
        return CrawlersHolder.getCrawler(ojName).getProblemInfo(problemId, author);
    }

    @Transactional(rollbackFor = Exception.class)
    public Problem adminAddOtherOJProblem(ProblemCrawler.RemoteProblemInfo remoteProblemInfo, String OJName) {

        Problem problem = remoteProblemInfo.getProblem();
        boolean addProblemResult = problemEntityService.save(problem);
        // 为新的其它oj题目添加对应的language
        QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
        languageQueryWrapper.eq("oj", OJName);
        List<Language> OJLanguageList = languageEntityService.list(languageQueryWrapper);
        List<ProblemLanguage> problemLanguageList = new LinkedList<>();

        for (Language language : OJLanguageList) {
            problemLanguageList.add(new ProblemLanguage().setPid(problem.getId()).setLid(language.getId()));
        }
        boolean addProblemLanguageResult = problemLanguageEntityService.saveOrUpdateBatch(problemLanguageList);

        boolean addProblemTagResult;
        List<Tag> addTagList = remoteProblemInfo.getTagList();

        List<Tag> needAddTagList = new LinkedList<>();

        HashMap<String, Tag> tagFlag = new HashMap<>();

        if (addTagList != null && addTagList.size() > 0) {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("oj", OJName);
            List<Tag> tagList = tagEntityService.list(tagQueryWrapper);
            // 已存在的tag不进行添加
            for (Tag hasTag : tagList) {
                tagFlag.put(hasTag.getName().toUpperCase(), hasTag);
            }
            for (Tag tmp : addTagList) {
                Tag tag = tagFlag.get(tmp.getName().toUpperCase());
                if (tag == null) {
                    tmp.setOj(OJName);
                    needAddTagList.add(tmp);
                } else {
                    needAddTagList.add(tag);
                }
            }
            tagEntityService.saveOrUpdateBatch(needAddTagList);

            List<ProblemTag> problemTagList = new LinkedList<>();
            for (Tag tmp : needAddTagList) {
                problemTagList.add(new ProblemTag().setTid(tmp.getId()).setPid(problem.getId()));
            }
            addProblemTagResult = problemTagEntityService.saveOrUpdateBatch(problemTagList);
        } else {
            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("name", OJName);
            Tag OJNameTag = tagEntityService.getOne(tagQueryWrapper, false);
            if (OJNameTag == null) {
                OJNameTag = new Tag();
                OJNameTag.setOj(OJName);
                OJNameTag.setName(OJName);
                tagEntityService.saveOrUpdate(OJNameTag);
            }
            addProblemTagResult = problemTagEntityService
                    .saveOrUpdate(new ProblemTag().setTid(OJNameTag.getId()).setPid(problem.getId()));
        }

        if (addProblemResult && addProblemTagResult && addProblemLanguageResult) {
            return problem;
        } else {
            return null;
        }
    }

}