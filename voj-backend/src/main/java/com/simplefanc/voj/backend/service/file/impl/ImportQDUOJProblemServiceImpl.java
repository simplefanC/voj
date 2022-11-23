package com.simplefanc.voj.backend.service.file.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusSystemErrorException;
import com.simplefanc.voj.backend.common.utils.MyFileUtil;
import com.simplefanc.voj.backend.dao.problem.LanguageEntityService;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.dao.problem.TagEntityService;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.pojo.dto.ProblemDto;
import com.simplefanc.voj.backend.pojo.dto.QDOJProblemDto;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.file.ImportQDUOJProblemService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.constants.*;
import com.simplefanc.voj.common.pojo.entity.problem.Language;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:47
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class ImportQDUOJProblemServiceImpl implements ImportQDUOJProblemService {

    public static final List<String> LANGUAGES = Arrays.asList("C", "C With O2", "C++", "C++ With O2", "Java", "Python3", "Python2", "Golang", "C#");
    private final LanguageEntityService languageEntityService;

    private final ProblemEntityService problemEntityService;

    private final TagEntityService tagEntityService;

    private final FilePathProperties filePathProps;

    /**
     * @param file
     * @MethodName importQDOJProblem
     * @Description zip文件导入题目 仅超级管理员可操作
     * @Return
     * @Since 2021/5/27
     */
    // TODO 行数过多
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importQDOJProblem(MultipartFile file) {
        String suffix = MyFileUtil.getFileSuffix(file);
        if (!"zip".toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请上传zip格式的题目文件压缩包！");
        }

        String fileDirId = IdUtil.simpleUUID();
        String fileDir = filePathProps.getTestcaseTmpFolder() + File.separator + fileDirId;
        String filePath = fileDir + File.separator + file.getOriginalFilename();
        // 文件夹不存在就新建
        FileUtil.mkdir(fileDir);
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            FileUtil.del(fileDir);
            throw new StatusSystemErrorException("服务器异常：qduoj题目上传失败！");
        }

        // 将压缩包压缩到指定文件夹
        ZipUtil.unzip(filePath, fileDir);

        // 删除zip文件
        FileUtil.del(filePath);

        // 检查文件是否存在
        File testCaseFileList = new File(fileDir);
        File[] files = testCaseFileList.listFiles();
        if (files == null || files.length == 0) {
            FileUtil.del(fileDir);
            throw new StatusFailException("评测数据压缩包里文件不能为空！");
        }

        HashMap<String, File> problemInfo = new HashMap<>();
        for (File tmp : files) {
            if (tmp.isDirectory()) {
                File[] problemAndTestcase = tmp.listFiles();
                if (problemAndTestcase == null || problemAndTestcase.length == 0) {
                    FileUtil.del(fileDir);
                    throw new StatusFailException("编号为：" + tmp.getName() + "的文件夹为空！");
                }
                for (File problemFile : problemAndTestcase) {
                    if (problemFile.isFile()) {
                        // 检查文件是否时json文件
                        if (!problemFile.getName().endsWith("json")) {
                            FileUtil.del(fileDir);
                            throw new StatusFailException("编号为：" + tmp.getName() + "的文件夹里面的题目数据格式错误，请使用json文件！");
                        }
                        problemInfo.put(tmp.getName(), problemFile);
                    }
                }
            }
        }

        // 读取json文件生成对象
        HashMap<String, QDOJProblemDto> problemVoMap = new HashMap<>();
        for (String key : problemInfo.keySet()) {
            try {
                FileReader fileReader = new FileReader(problemInfo.get(key));
                JSONObject problemJson = JSONUtil.parseObj(fileReader.readString());
                QDOJProblemDto qdojProblemDto = QDOJProblemToProblemVo(problemJson);
                problemVoMap.put(key, qdojProblemDto);
            } catch (Exception e) {
                FileUtil.del(fileDir);
                throw new StatusFailException("请检查编号为：" + key + "的题目json文件的格式：" + e.getLocalizedMessage());
            }
        }

        QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
        languageQueryWrapper.eq("oj", Constant.LOCAL);
        List<Language> languageList = languageEntityService.list(languageQueryWrapper);

        HashMap<String, Long> languageMap = new HashMap<>();
        for (Language language : languageList) {
            languageMap.put(language.getName(), language.getId());
        }

        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        List<Tag> tagList = tagEntityService.list(new QueryWrapper<Tag>().eq("oj", Constant.LOCAL));
        HashMap<String, Tag> tagMap = new HashMap<>();
        for (Tag tag : tagList) {
            tagMap.put(tag.getName().toUpperCase(), tag);
        }

        List<ProblemDto> problemDtos = new LinkedList<>();
        for (String key : problemInfo.keySet()) {
            QDOJProblemDto qdojProblemDto = problemVoMap.get(key);
            // 格式化题目语言
            List<Language> languages = new LinkedList<>();
            for (String lang : qdojProblemDto.getLanguages()) {
                Long lid = languageMap.getOrDefault(lang, null);
                languages.add(new Language().setId(lid).setName(lang));
            }

            // 格式化标签
            List<Tag> tags = new LinkedList<>();
            for (String tagStr : qdojProblemDto.getTags()) {
                Tag tag = tagMap.getOrDefault(tagStr.toUpperCase(), null);
                if (tag == null) {
                    tags.add(new Tag().setName(tagStr).setOj(Constant.LOCAL));
                } else {
                    tags.add(tag);
                }
            }

            Problem problem = qdojProblemDto.getProblem();
            if (problem.getAuthor() == null) {
                problem.setAuthor(userRolesVo.getUsername());
            }
            ProblemDto problemDto = new ProblemDto();

            String mode = JudgeMode.DEFAULT.getMode();
            if (qdojProblemDto.getIsSpj()) {
                mode = JudgeMode.SPJ.getMode();
            }

            problemDto.setJudgeMode(mode).setProblem(problem).setCodeTemplates(qdojProblemDto.getCodeTemplates())
                    .setTags(tags).setLanguages(languages)
                    .setUploadTestcaseDir(fileDir + File.separator + key + File.separator + "testcase")
                    .setIsUploadTestCase(true).setSamples(qdojProblemDto.getSamples());

            problemDtos.add(problemDto);
        }
        for (ProblemDto problemDto : problemDtos) {
            problemEntityService.adminAddProblem(problemDto);
        }
    }

    private QDOJProblemDto QDOJProblemToProblemVo(JSONObject problemJson) {
        QDOJProblemDto qdojProblemDto = new QDOJProblemDto();
        List<String> tags = (List<String>) problemJson.get("tags");
        qdojProblemDto.setTags(tags.stream().map(UnicodeUtil::toString).collect(Collectors.toList()));
        qdojProblemDto.setLanguages(LANGUAGES);
        Object spj = problemJson.getObj("spj");
        boolean isSpj = !JSONUtil.isNull(spj);
        qdojProblemDto.setIsSpj(isSpj);

        Problem problem = new Problem();
        if (isSpj) {
            JSONObject spjJson = JSONUtil.parseObj(spj);
            problem.setSpjCode(spjJson.getStr("code")).setSpjLanguage(spjJson.getStr("language"));
        }
        problem.setAuth(ProblemEnum.AUTH_PUBLIC.getCode())
                .setIsUploadCase(true).setSource(problemJson.getStr("source", null))
                .setDifficulty(ProblemLevelEnum.PROBLEM_LEVEL_MID.getCode())
                .setProblemId(problemJson.getStr("display_id")).setIsRemoveEndBlank(true).setOpenCaseResult(true)
                .setCodeShare(false).setType("ACM".equals(problemJson.getStr("rule_type")) ? ContestEnum.TYPE_ACM.getCode() : ContestEnum.TYPE_OI.getCode())
                .setTitle(problemJson.getStr("title"))
                .setDescription(UnicodeUtil.toString(problemJson.getJSONObject("description").getStr("value")))
                .setInput(UnicodeUtil.toString(problemJson.getJSONObject("input_description").getStr("value")))
                .setOutput(UnicodeUtil.toString(problemJson.getJSONObject("output_description").getStr("value")))
                .setHint(UnicodeUtil.toString(problemJson.getJSONObject("hint").getStr("value")))
                .setTimeLimit(problemJson.getInt("time_limit")).setMemoryLimit(problemJson.getInt("memory_limit"));

        JSONArray samples = problemJson.getJSONArray("samples");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < samples.size(); i++) {
            JSONObject sample = (JSONObject) samples.get(i);
            String input = sample.getStr("input");
            String output = sample.getStr("output");
            sb.append("<input>").append(input).append("</input>");
            sb.append("<output>").append(output).append("</output>");
        }
        problem.setExamples(sb.toString());

        int sumScore = 0;
        JSONArray testcaseList = problemJson.getJSONArray("test_case_score");
        List<ProblemCase> problemSamples = new LinkedList<>();
        for (int i = 0; i < testcaseList.size(); i++) {
            JSONObject testcase = (JSONObject) testcaseList.get(i);
            String input = testcase.getStr("input_name");
            String output = testcase.getStr("output_name");
            Integer score = testcase.getInt("score", null);
            problemSamples.add(new ProblemCase().setInput(input).setOutput(output).setScore(score));
            if (score != null) {
                sumScore += score;
            }
        }
        problem.setIsRemote(false);
        problem.setOiScore(sumScore);
        qdojProblemDto.setSamples(problemSamples);
        qdojProblemDto.setProblem(problem);
        return qdojProblemDto;

    }

}