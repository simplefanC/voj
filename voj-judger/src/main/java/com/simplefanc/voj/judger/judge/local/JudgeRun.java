package com.simplefanc.voj.judger.judge.local;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.common.constants.JudgeMode;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.judger.common.constants.JudgeDir;
import com.simplefanc.voj.judger.common.constants.RunConfig;
import com.simplefanc.voj.judger.common.exception.SystemError;
import com.simplefanc.voj.judger.common.utils.JudgeUtil;
import com.simplefanc.voj.judger.common.utils.ThreadPoolUtil;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeDTO;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeGlobalDTO;
import com.simplefanc.voj.judger.judge.local.task.DefaultJudge;
import com.simplefanc.voj.judger.judge.local.task.InteractiveJudge;
import com.simplefanc.voj.judger.judge.local.task.SpecialJudge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @Author: chenfan
 * @Date: 2021/4/16 12:15
 * @Description: 判题流程解耦重构3.0，该类负责输入数据进入程序进行测评
 */
@Component
@RequiredArgsConstructor
public class JudgeRun {

    private final DefaultJudge defaultJudge;

    private final SpecialJudge specialJudge;

    private final InteractiveJudge interactiveJudge;

    private final ProblemTestCaseUtils problemTestCaseUtils;

    public List<JSONObject> judgeAllCase(Judge judge, Problem problem, String userFileId, String userFileSrc, Boolean getUserOutput)
            throws SystemError, ExecutionException, InterruptedException, UnsupportedEncodingException {

        JudgeGlobalDTO judgeGlobalDTO = getJudgeGlobalDTO(judge, problem, userFileId, userFileSrc, getUserOutput);

        List<FutureTask<JSONObject>> futureTasks = getFutureTasks(judgeGlobalDTO);

        // 提交到线程池进行执行
        for (FutureTask<JSONObject> futureTask : futureTasks) {
            ThreadPoolUtil.getInstance().getThreadPool().submit(futureTask);
        }

        List<JSONObject> result = new LinkedList<>();
        while (futureTasks.size() > 0) {
            Iterator<FutureTask<JSONObject>> iterable = futureTasks.iterator();
            // 遍历一遍
            while (iterable.hasNext()) {
                FutureTask<JSONObject> future = iterable.next();
                if (future.isDone() && !future.isCancelled()) {
                    // 获取线程返回结果 TODO 报OOM异常
                    result.add(future.get());
                    // 任务完成移除任务
                    iterable.remove();
                } else {
                    // 避免CPU高速运转，这里休息10ms
                    Thread.sleep(10);
                }
            }
        }
        return result;
    }

    private List<FutureTask<JSONObject>> getFutureTasks(JudgeGlobalDTO judgeGlobalDTO) {
        List<FutureTask<JSONObject>> futureTasks = new ArrayList<>();
        final JSONArray testcaseList = (JSONArray) judgeGlobalDTO.getTestCaseInfo().get("testCases");
        for (int index = 0; index < testcaseList.size(); index++) {
            JSONObject testcase = (JSONObject) testcaseList.get(index);
            final int testCaseId = index + 1;
            // 输入文件名
            final String inputFileName = testcase.getStr("inputName");
            // 输出文件名
            final String outputFileName = testcase.getStr("outputName");
            // 题目数据的输入文件的路径
            final String testCaseInputPath = judgeGlobalDTO.getTestCasesDir() + File.separator + inputFileName;
            // 题目数据的输出文件的路径
            final String testCaseOutputPath = judgeGlobalDTO.getTestCasesDir() + File.separator + outputFileName;
            // 数据库表的测试样例id
            final Long caseId = testcase.getLong("caseId", null);
            // 该测试点的满分
            final Integer score = testcase.getInt("score", 0);

            final Long maxOutputSize = Math.max(testcase.getLong("outputSize", 0L) * 2, 16 * 1024 * 1024L);

            JudgeDTO judgeDTO = JudgeDTO.builder().testCaseId(testCaseId).testCaseInputPath(testCaseInputPath)
                    .testCaseOutputPath(testCaseOutputPath).maxOutputSize(maxOutputSize).build();
            // 将每个需要测试的线程任务加入任务列表中
            futureTasks.add(new FutureTask<>(
                    new JudgeTask(judgeDTO, judgeGlobalDTO, caseId, score, inputFileName, outputFileName)));
        }
        return futureTasks;
    }

    private JudgeGlobalDTO getJudgeGlobalDTO(Judge judge, Problem problem, String userFileId, String userFileSrc, Boolean getUserOutput) throws SystemError, UnsupportedEncodingException {
        Long submitId = judge.getSubmitId();
        String judgeLanguage = judge.getLanguage();

        // 默认给题目限制时间+200ms用来测评
        Long testTime = (long) problem.getTimeLimit() + 200;

        JudgeMode judgeMode = JudgeMode.getJudgeMode(problem.getJudgeMode());

        if (judgeMode == null) {
            throw new RuntimeException(
                    "The judge mode of problem " + problem.getProblemId() + " error:" + problem.getJudgeMode());
        }

        // 从文件中加载测试数据json
        JSONObject testCasesInfo = problemTestCaseUtils.loadTestCaseInfo(problem);
        if (testCasesInfo == null) {
            throw new SystemError("The evaluation data of the problem does not exist", null, null);
        }

        // 测试数据文件所在文件夹
        String testCasesDir = JudgeDir.TEST_CASE_DIR + File.separator + "problem_" + problem.getId();

        // 用户输出的文件夹
        String runDir = JudgeDir.RUN_WORKPLACE_DIR + File.separator + submitId;

        RunConfig runConfig = RunConfig.getRunnerByLanguage(judgeLanguage);
        RunConfig spjConfig = RunConfig.getRunnerByLanguage("SPJ-" + problem.getSpjLanguage());
        RunConfig interactiveConfig = RunConfig.getRunnerByLanguage("INTERACTIVE-" + problem.getSpjLanguage());

        return JudgeGlobalDTO.builder()
                .problemId(problem.getId())
                .judgeMode(judgeMode)
                .userFileId(userFileId)
                .userFileSrc(userFileSrc)
                .runDir(runDir)
                .testTime(testTime)
                .maxMemory((long) problem.getMemoryLimit())
                .maxTime((long) problem.getTimeLimit())
                .maxStack(problem.getStackLimit())
                .testCasesDir(testCasesDir)
                .testCaseInfo(testCasesInfo)
                .judgeExtraFiles(JudgeUtil.getProblemExtraFileMap(problem, "judge"))
                .runConfig(runConfig)
                .spjRunConfig(spjConfig)
                .interactiveRunConfig(interactiveConfig)
                .needUserOutputFile(getUserOutput)
                .removeEOLBlank(problem.getIsRemoveEndBlank()).build();
    }

    class JudgeTask implements Callable<JSONObject> {
        JudgeDTO judgeDTO;
        JudgeGlobalDTO judgeGlobalDTO;
        Long caseId;
        Integer score;
        String inputFileName;
        String outputFileName;

        public JudgeTask(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO, Long caseId,
                         Integer score, String inputFileName, String outputFileName) {
            this.judgeDTO = judgeDTO;
            this.judgeGlobalDTO = judgeGlobalDTO;
            this.caseId = caseId;
            this.score = score;
            this.inputFileName = inputFileName;
            this.outputFileName = outputFileName;
        }

        @Override
        public JSONObject call() throws Exception {
            JSONObject result;
            switch (judgeGlobalDTO.getJudgeMode()) {
                case DEFAULT:
                    result = defaultJudge.judgeCase(judgeDTO, judgeGlobalDTO);
                    break;
                case SPJ:
                    result = specialJudge.judgeCase(judgeDTO, judgeGlobalDTO);
                    break;
                case INTERACTIVE:
                    result = interactiveJudge.judgeCase(judgeDTO, judgeGlobalDTO);
                    break;
                default:
                    throw new RuntimeException("The problem mode is error:" + judgeGlobalDTO.getJudgeMode());
            }
            result.set("caseId", caseId);
            result.set("score", score);
            result.set("inputFileName", inputFileName);
            result.set("outputFileName", outputFileName);
            return result;
        }

    }

}