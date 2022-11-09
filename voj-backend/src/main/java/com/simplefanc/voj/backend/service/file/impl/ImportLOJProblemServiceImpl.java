package com.simplefanc.voj.backend.service.file.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusNotFoundException;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.dao.problem.LanguageEntityService;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.judge.remote.crawler.ProblemCrawler;
import com.simplefanc.voj.backend.pojo.dto.ProblemDto;
import com.simplefanc.voj.backend.service.file.ImportLOJProblemService;
import com.simplefanc.voj.common.constants.Constant;
import com.simplefanc.voj.common.pojo.entity.problem.Language;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class ImportLOJProblemServiceImpl implements ImportLOJProblemService {
    public static final String JUDGE_NAME = "LOJ";
    private static final String GET_PROBLEM_URL = "https://api.loj.ac.cn/api/problem/getProblem";
    private static final String DOWNLOAD_PROBLEM_FILES_URL = "https://api.loj.ac.cn/api/problem/downloadProblemFiles";

    private final FilePathProperties filePathProps;
    private final LanguageEntityService languageEntityService;
    private final ProblemEntityService problemEntityService;


    public boolean importLOJProblem(Integer problemId) {
        Problem problem = problemEntityService.lambdaQuery()
                .eq(Problem::getProblemId, JUDGE_NAME + "-" + problemId)
                .one();
        if (problem != null) {
            throw new StatusFailException("该题目已添加，请勿重复添加！");
        }
        return problemEntityService.adminAddProblem(getProblemDto(problemId));
    }

    private ProblemDto getProblemDto(Integer problemId) {
        ProblemDto problemDto = new ProblemDto();

        Map<String, Object> map = MapUtil.builder(new HashMap<String, Object>())
                .put("displayId", problemId)
                .put("localizedContentsOfLocale", "zh_CN")
                .put("tagsOfLocale", "zh_CN")
                .put("samples", true)
                .put("judgeInfo", true)
                .put("judgeInfoToBePreprocessed", true)
                .put("statistics", true)
                .put("discussionCount", true)
                .put("permissionOfCurrentUser", true)
                .put("lastSubmissionAndLastAcceptedSubmission", true).build();
        String json = JSONUtil.toJsonStr(map);
        String body = HttpRequest.post(GET_PROBLEM_URL).body(json).execute().body();
        LOJProblem lojProblem = JSONUtil.toBean(body, LOJProblem.class);
        Contents contents = lojProblem.getLocalizedContentsOfLocale();
        if (contents == null) {
            throw new StatusNotFoundException("该题号对应的题目不存在");
        }
        JudgeInfo judgeInfo = lojProblem.getJudgeInfo();
        List<Sample> samples = lojProblem.getSamples();
        // 设置 tag
        List<LOJTag> tags = lojProblem.getTagsOfLocale();
        List<Tag> tagList = tags.stream().map(lojTag -> new Tag().setName(lojTag.name)).collect(Collectors.toList());
        problemDto.setTags(tagList);

        ProblemCrawler.RemoteProblemInfo problemInfo = new ProblemCrawler.RemoteProblemInfo();
        problemInfo.setTagList(tagList);
        Problem problem = problemInfo.getProblem();
        String examples = samples.stream().map(sample -> "<input>" + sample.getInputData() + "</input>" +
                        "<output>" + sample.getOutputData() + "</output>")
                .collect(Collectors.joining(""));
        problem.setIsRemote(false)
                .setProblemId(JUDGE_NAME + "-" + problemId)
                .setTitle(contents.title)
                .setTimeLimit(judgeInfo.timeLimit)
                .setMemoryLimit(judgeInfo.memoryLimit)
                .setDescription(contents.contentSections.get(0).text)
                .setInput(contents.contentSections.get(1).text)
                .setOutput(contents.contentSections.get(2).text)
                .setExamples(examples)
                .setHint(contents.contentSections.get(4).text)
                .setSource(String.format("<a style='color:#1A5CC8' href='https://loj.ac/p/%d'>%s</a>", problemId, JUDGE_NAME + "-" + problemId));
        // 设置 Problem
        problemDto.setProblem(problem);

        // 设置 测试用例
        downloadProblemFiles(problemDto, problemId, judgeInfo);

        // 设置题目语言
        List<Language> languages = languageEntityService.lambdaQuery().eq(Language::getOj, Constant.LOCAL).list();
        problemDto.setLanguages(languages);

        return problemDto;
    }

    private void downloadProblemFiles(ProblemDto problemDto, Integer problemId, JudgeInfo judgeInfo) {
        // 下载测试用例到本地
        // 多线程
        List<JudgeInfo.Task.Testcase> testcases = judgeInfo.subtasks.get(0).testcases;
        // 每个Testcase需要下载 inputFile 和 outputFile
        List<String> filenameList = new ArrayList<>();
        testcases.forEach(testcase -> {
            filenameList.add(testcase.inputFile);
            filenameList.add(testcase.outputFile);
        });
        Map<String, Object> map = MapUtil.builder(new HashMap<String, Object>())
                .put("problemId", problemId)
                .put("type", "TestData")
                .put("filenameList", filenameList).build();
        String json = JSONUtil.toJsonStr(map);
        String body = HttpRequest.post(DOWNLOAD_PROBLEM_FILES_URL).body(json).execute().body();
        DownloadInfo downloadInfo = JSONUtil.toBean(body, DownloadInfo.class);
        List<DownloadInfo.Info> downloadInfos = downloadInfo.downloadInfo;
        CompletableFuture[] futures = new CompletableFuture[downloadInfos.size()];
        // 使用线程池
        ExecutorService threadPool = new ThreadPoolExecutor(
                // 核心线程数
                2,
                // 最大线程数。最多几个线程并发。
                4,
                // 当非核心线程无任务时，几秒后结束该线程
                3,
                // 结束线程时间单位
                TimeUnit.SECONDS,
                // 阻塞队列，限制等候线程数
                new LinkedBlockingDeque<>(200), Executors.defaultThreadFactory(),
                // 队列满了，尝试去和最早的竞争，也不会抛出异常！
                new ThreadPoolExecutor.DiscardOldestPolicy());
        String fileDir = filePathProps.getTestcaseTmpFolder() + File.separator + IdUtil.simpleUUID();
        // 新建文件夹
        FileUtil.mkdir(fileDir);
        for (int i = 0; i < downloadInfos.size(); i++) {
            DownloadInfo.Info info = downloadInfos.get(i);
            futures[i] = CompletableFuture.supplyAsync(() ->
                            HttpUtil.downloadFile(info.downloadUrl, FileUtil.file(fileDir + File.separator + info.filename)),
                    threadPool);
        }

        // allOf() 方法会等到所有的 CompletableFuture 都运行完成之后再返回
        CompletableFuture<Void> headerFuture = CompletableFuture.allOf(futures);
        // 都运行完了之后再继续执行
        headerFuture.join();
        // 关闭线程池
        threadPool.shutdownNow();

        List<ProblemCase> problemCaseList = testcases.stream()
                .map(testcase -> new ProblemCase().setInput(testcase.inputFile).setOutput(testcase.outputFile))
                .collect(Collectors.toList());
        problemDto.setUploadTestcaseDir(fileDir)
                .setIsUploadTestCase(true)
                .setSamples(problemCaseList);
//        List<Long> res = new ArrayList<>();
//        for (int i = 0; i < downloadInfos.size(); i++) {
//            res.add((Long) futures[i].get());
//        }
    }
}


@Data
class LOJProblem {
    Contents localizedContentsOfLocale;

    JudgeInfo judgeInfo;

    List<Sample> samples;

    List<LOJTag> tagsOfLocale;
}

@Data
class Contents {
    String title;
    List<Section> contentSections;

    @Data
    class Section {
        String sectionTitle;
        String type;
        String text;
    }
}

@Data
class JudgeInfo {
    Integer timeLimit;
    Integer memoryLimit;
    List<Task> subtasks;

    @Data
    class Task {
        List<Testcase> testcases;

        @Data
        class Testcase {
            String inputFile;
            String outputFile;
        }
    }
}

@Data
class Sample {
    String inputData;
    String outputData;
}

@Data
class LOJTag {
    String name;
}

///////////////////////////////////
@Data
class DownloadInfo {
    List<Info> downloadInfo;

    @Data
    class Info {
        String filename;
        String downloadUrl;
    }
}
