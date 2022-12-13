package com.simplefanc.voj.backend.service.file.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.ReUtil;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.dao.problem.LanguageEntityService;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.pojo.dto.ProblemDTO;
import com.simplefanc.voj.backend.service.file.ImportDSOJProblemService;
import com.simplefanc.voj.common.constants.*;
import com.simplefanc.voj.common.pojo.entity.problem.Language;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:47
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class ImportDSOJProblemServiceImpl implements ImportDSOJProblemService {

    private final LanguageEntityService languageEntityService;

    private final ProblemEntityService problemEntityService;

    private final FilePathProperties filePathProps;

    private static final Pattern EXAMPLE_PATTERN = Pattern.compile("[#\\s]*?输入[\\s\\S]*?```\r\n([\\s\\S]*?)[\\r\\n]*?```[\\s\\S]*?[#\\s]*?输出[\\s\\S]*?```\r\n([\\s\\S]*?)\r\n```");
    private static final Pattern TAG_PATTERN = Pattern.compile("<option value=\"\\d*\" selected>([\\s\\S]*?)</option>");

    public static void main(String[] args) {
//        String fileDir = "/Users/simplefanc/Downloads/test/dsoj/data";
////        String fileDir = "/Users/simplefanc/Downloads/test/dsoj/html";
//        File file = new File(fileDir);
//        File[] files = file.listFiles();
//        for (File tmp : files) {
//            String name = tmp.getName();
//            int i = name.lastIndexOf(".");
//            String prefix = name.substring(0, i);
//            String suffix = name.substring(i + 1);
//            if ("zip".equals(suffix)) {
//                // 文件夹不存在就新建
//                String dirPath = fileDir + File.separator + prefix;
//                FileUtil.mkdir(dirPath);
//                // 将压缩包压缩到指定文件夹
//                try {
//                    ZipUtil.unzip(tmp.getPath(), dirPath);
//                } catch (Exception e) {
//                    System.out.println(prefix);
//                }
//            } else if ("html".equals(suffix)) {
//
//            }
//        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void importDSOJProblem() {
//        String fileDir = "/Users/simplefanc/Downloads/test/dsoj";
        String fileDir = filePathProps.getTestcaseTmpFolder() + File.separator + "dsoj";
        // 检查文件是否存在
        File fileList = new File(fileDir);
        File[] files = fileList.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                int idx = name.lastIndexOf(".");
                String prefix = name.substring(0, idx);
                String suffix = name.substring(idx + 1);
                if ("html".equals(suffix)) {
                    // 读取html文件
                    String content = FileUtil.readString(file.getPath(), StandardCharsets.UTF_8);
                    ProblemDTO problemDTO = getProblemDTO(prefix, content);

                    processTestCase(fileDir, prefix, problemDTO);
                    problemEntityService.adminAddProblem(problemDTO);
                }
            }
        }
    }

    private void processTestCase(String fileDir, String prefix, ProblemDTO problemDTO) {
        // 检查文件是否存在
        String testCaseFileDir = fileDir + File.separator + prefix;
        File testCaseFileList = new File(testCaseFileDir);
        File[] testCaseFiles = testCaseFileList.listFiles();
        if (testCaseFiles == null || testCaseFiles.length == 0) {
            System.err.println(prefix);
            throw new StatusFailException("评测数据压缩包里文件不能为空！");
        }

        HashMap<String, String> inputData = new HashMap<>();
        HashMap<String, String> outputData = new HashMap<>();

        // 遍历读取与检查是否in和out文件一一对应，否则报错
        for (File tmp : testCaseFiles) {
            String tmpPreName;
            if (tmp.getName().endsWith(".in")) {
                tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf(".in"));
                inputData.put(tmpPreName, tmp.getName());
            } else if (tmp.getName().endsWith(".out")) {
                tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf(".out"));
                outputData.put(tmpPreName, tmp.getName());
            } else if (tmp.getName().endsWith(".ans")) {
                tmpPreName = tmp.getName().substring(0, tmp.getName().lastIndexOf(".ans"));
                outputData.put(tmpPreName, tmp.getName());
            }
        }

        // 设置 测试用例
        List<ProblemCase> testCaseList = new ArrayList<>();
        // 进行数据对应检查,同时生成返回数据
        for (String key : inputData.keySet()) {
            String inputFileName = inputData.get(key);
            // 若有名字对应的out文件不存在的，直接生成对应的out文件
            final String outputFileName = outputData.getOrDefault(key, null);
            if (outputFileName == null) {
                FileWriter fileWriter = new FileWriter(testCaseFileDir + File.separator + key + ".out");
                fileWriter.write("");
            }
            testCaseList.add(new ProblemCase().setInput(inputFileName).setOutput(outputFileName));
        }
        int averageScore = 100 / testCaseList.size();
        int add1Num = 100 - averageScore * testCaseList.size();
        for (int i = 0; i < testCaseList.size(); i++) {
            if (i >= testCaseList.size() - add1Num) {
                testCaseList.get(i).setScore(averageScore + 1);
            } else {
                testCaseList.get(i).setScore(averageScore);
            }
        }
        testCaseList = testCaseList.stream().sorted((o1, o2) -> {
            String a = o1.getInput().split("\\.")[0];
            String b = o2.getInput().split("\\.")[0];
            if (a.length() > b.length()) {
                return 1;
            }
            if (a.length() < b.length()) {
                return -1;
            }
            return a.compareTo(b);
        }).collect(Collectors.toList());

        problemDTO.setUploadTestcaseDir(testCaseFileDir)
                .setIsUploadTestCase(true)
                .setSamples(testCaseList);
    }

    private ProblemDTO getProblemDTO(String id, String content) {
        String description = ReUtil.getGroup1("<textarea class=\"markdown-edit\" rows=\"15\" id=\"description\" name=\"description\">([\\s\\S]*?)<\\/textarea>",
                content);
        String title = ReUtil.getGroup1("<input class=\"font-content\" type=\"text\" id=\"title\" name=\"title\" value=\"([\\s\\S]*?)\">",
                content);
        String inputFormat = ReUtil.getGroup1("<textarea class=\"markdown-edit\" rows=\"10\" id=\"input\" name=\"input_format\">([\\s\\S]*?)<\\/textarea>",
                content);
        String outputFormat = ReUtil.getGroup1("<textarea class=\"markdown-edit\" rows=\"10\" id=\"output\" name=\"output_format\">([\\s\\S]*?)<\\/textarea>",
                content);
//        String example = ReUtil.getGroup1("<textarea class=\"markdown-edit\" rows=\"15\" id=\"example\" name=\"example\">([\\s\\S]*?)<\\/textarea>",
//                content);

        String hint = ReUtil.getGroup1("<textarea class=\"markdown-edit\" rows=\"10\" id=\"hint\" name=\"limit_and_hint\">([\\s\\S]*?)<\\/textarea>",
                content);
//        String tags = ReUtil.getGroup1("<option value=\"\\d*\" selected>([\\s\\S]*?)<\\/option>",
//                content);

        // 设置 tag
        Matcher tagMatcher = TAG_PATTERN.matcher(content);
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag().setName("DSOJ"));
        while (tagMatcher.find()) {
            tagList.add(new Tag().setName(tagMatcher.group(1)));
        }

        Matcher exampleMatcher = EXAMPLE_PATTERN.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (exampleMatcher.find()) {
            sb.append("<input>").append(exampleMatcher.group(1)).append("</input>")
                    .append("<output>").append(exampleMatcher.group(2)).append("</output>");
        }

        Problem problem = Problem.builder()
                .isRemote(true)
                .type(ContestEnum.TYPE_ACM.getCode())
                .auth(ProblemEnum.AUTH_PUBLIC.getCode())
                .author("root")
                .openCaseResult(false)
                .isRemoveEndBlank(false)
                .difficulty(ProblemLevelEnum.PROBLEM_LEVEL_MID.getCode())
                .isRemote(false)
                .problemId("DSOJ-" + id)
                .title(title)
                .description(description)
                .input(inputFormat)
                .output(outputFormat)
                .examples(sb.toString())
                .openCaseResult(true)
                .isRemoveEndBlank(true)
                // 数据范围及提示
                .hint(hint)
                .build();

        List<Language> languages = languageEntityService.lambdaQuery().eq(Language::getOj, Constant.LOCAL).list();

        return new ProblemDTO()
                // 设置 Problem
                .setProblem(problem)
                // 设置题目语言
                .setLanguages(languages)
                .setTags(tagList)
                .setJudgeMode(JudgeMode.DEFAULT.getMode());
    }
}