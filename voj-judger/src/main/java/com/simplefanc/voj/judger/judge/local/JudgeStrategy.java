package com.simplefanc.voj.judger.judge.local;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.constants.JudgeMode;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.judge.JudgeCase;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.judger.common.constants.CompileConfig;
import com.simplefanc.voj.judger.common.constants.JudgeDir;
import com.simplefanc.voj.judger.common.exception.CompileError;
import com.simplefanc.voj.judger.common.exception.SubmitError;
import com.simplefanc.voj.judger.common.exception.SystemError;
import com.simplefanc.voj.judger.common.utils.JudgeUtil;
import com.simplefanc.voj.judger.dao.JudgeCaseEntityService;
import com.simplefanc.voj.judger.dao.JudgeEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Slf4j(topic = "voj")
@Component
@RequiredArgsConstructor
public class JudgeStrategy {

    private final JudgeEntityService judgeEntityService;

    private final JudgeCaseEntityService JudgeCaseEntityService;

    private final JudgeRun judgeRun;

    public HashMap<String, Object> judge(Problem problem, Judge judge) {
        HashMap<String, Object> result = new HashMap<>();
        // 编译好的临时代码文件id
        String userFileId = null;
        String userFileSrc = null;
        try {
            // 对用户源代码进行编译 获取tmpfs中的fileId
            CompileConfig compileConfig = CompileConfig.getCompilerByLanguage(judge.getLanguage());
            // 有的语言可能不支持编译
            if (compileConfig != null) {
                userFileId = Compiler.compile(compileConfig, judge.getCode(), judge.getLanguage(),
                        JudgeUtil.getProblemExtraFileMap(problem, "user"));
            } else {
                // 目前只有js、php不支持编译，需要提供源代码文件的绝对路径
                userFileSrc = JudgeDir.RUN_WORKPLACE_DIR + File.separator + problem.getId() + File.separator
                        + getUserFileName(judge.getLanguage());
                FileWriter fileWriter = new FileWriter(userFileSrc);
                fileWriter.write(judge.getCode());
            }

            // 检查是否为spj或者interactive，同时是否有对应编译完成的文件，若不存在，就先编译生成该文件，同时也要检查版本
            boolean isOk = checkOrCompileExtraProgram(problem);
            if (!isOk) {
                result.put("code", JudgeStatus.STATUS_SYSTEM_ERROR.getStatus());
                result.put("errMsg", "The special judge or interactive program code does not exist.");
                result.put("time", 0);
                result.put("memory", 0);
                return result;
            }

            // 更新状态为评测数据中
            judge.setStatus(JudgeStatus.STATUS_JUDGING.getStatus());
            judgeEntityService.updateById(judge);
            // 开始测试每个测试点
            List<JSONObject> allCaseResultList = judgeRun.judgeAllCase(judge, problem, userFileId, userFileSrc, false);

            // 对全部测试点结果进行评判，获取最终评判结果
            return getJudgeInfo(allCaseResultList, problem, judge);
        } catch (SystemError systemError) {
            handleSystemError(problem, judge, result, systemError);
        } catch (SubmitError submitError) {
            handleSubmitError(problem, judge, result, submitError);
        } catch (CompileError compileError) {
            handleCompileError(result, compileError);
        } catch (Exception e) {
            handleOtherException(problem, judge, result, e);
        } finally {
            // 删除tmpfs内存中的用户代码可执行文件
            if (!StrUtil.isEmpty(userFileId)) {
                SandboxRun.delFile(userFileId);
            }
        }
        return result;
    }

    private void handleOtherException(Problem problem, Judge judge, HashMap<String, Object> result, Exception e) {
        result.put("code", JudgeStatus.STATUS_SYSTEM_ERROR.getStatus());
        result.put("errMsg",
                "Oops, something has gone wrong with the judgeServer. Please report this to administrator.");
        result.put("time", 0);
        result.put("memory", 0);
        log.error("题号为：" + problem.getId() + "的题目，提交id为" + judge.getSubmitId()
                + "在评测过程中发生系统性的异常-------------------->", e);
    }

    private void handleCompileError(HashMap<String, Object> result, CompileError compileError) {
        result.put("code", JudgeStatus.STATUS_COMPILE_ERROR.getStatus());
        result.put("errMsg", mergeNonEmptyStrings(compileError.getStdout(), compileError.getStderr()));
        result.put("time", 0);
        result.put("memory", 0);
    }

    private void handleSubmitError(Problem problem, Judge judge, HashMap<String, Object> result, SubmitError submitError) {
        result.put("code", JudgeStatus.STATUS_SUBMITTED_FAILED.getStatus());
        result.put("errMsg",
                mergeNonEmptyStrings(submitError.getMessage(), submitError.getStdout(), submitError.getStderr()));
        result.put("time", 0);
        result.put("memory", 0);
        log.error(
                "题号为：" + problem.getId() + "的题目，提交id为" + judge.getSubmitId() + "在评测过程中发生提交的异常-------------------->",
                submitError);
    }

    private void handleSystemError(Problem problem, Judge judge, HashMap<String, Object> result, SystemError systemError) {
        result.put("code", JudgeStatus.STATUS_SYSTEM_ERROR.getStatus());
        result.put("errMsg",
                "Oops, something has gone wrong with the judgeServer. Please report this to administrator.");
        result.put("time", 0);
        result.put("memory", 0);
        log.error(
                "题号为：" + problem.getId() + "的题目，提交id为" + judge.getSubmitId() + "在评测过程中发生系统性的异常------------------->",
                systemError);
    }

    private Boolean checkOrCompileExtraProgram(Problem problem) throws CompileError, SystemError {
        JudgeMode judgeMode = JudgeMode.getJudgeMode(problem.getJudgeMode());
        String currentVersion = problem.getCaseVersion();
        Boolean isOk;
        switch (Objects.requireNonNull(judgeMode)) {
            case DEFAULT:
                return true;
            case SPJ:
                isOk = isCompileSpjOk(problem, currentVersion);
                if (isOk != null) return isOk;
                break;
            case INTERACTIVE:
                isOk = isCompileInteractive(problem, currentVersion);
                if (isOk != null) return isOk;
                break;
            default:
                throw new RuntimeException("The problem mode is error:" + judgeMode);
        }

        return true;
    }

    private Boolean isCompileInteractive(Problem problem, String currentVersion) throws SystemError {
        CompileConfig compiler;
        String programFilePath;
        String programVersionPath;
        compiler = CompileConfig.getCompilerByLanguage("INTERACTIVE-" + problem.getSpjLanguage());
        programFilePath = JudgeDir.INTERACTIVE_WORKPLACE_DIR + File.separator + problem.getId() + File.separator
                + compiler.getExeName();

        programVersionPath = JudgeDir.INTERACTIVE_WORKPLACE_DIR + File.separator + problem.getId() + File.separator
                + "version";

        // 如果不存在该已经编译好的程序，则需要再次进行编译 版本变动也需要重新编译
        if (!FileUtil.exist(programFilePath) || !FileUtil.exist(programVersionPath)) {
            boolean isCompileInteractive = Compiler.compileInteractive(problem.getSpjCode(), problem.getId(),
                    problem.getSpjLanguage(), JudgeUtil.getProblemExtraFileMap(problem, "judge"));
            FileWriter fileWriter = new FileWriter(programVersionPath);
            fileWriter.write(currentVersion);
            return isCompileInteractive;
        }

        FileReader interactiveVersionFileReader = new FileReader(programVersionPath);
        String recordInteractiveVersion = interactiveVersionFileReader.readString();

        // 版本变动也需要重新编译
        if (!currentVersion.equals(recordInteractiveVersion)) {
            boolean isCompileInteractive = Compiler.compileSpj(problem.getSpjCode(), problem.getId(),
                    problem.getSpjLanguage(), JudgeUtil.getProblemExtraFileMap(problem, "judge"));

            FileWriter fileWriter = new FileWriter(programVersionPath);
            fileWriter.write(currentVersion);

            return isCompileInteractive;
        }
        return null;
    }


    private Boolean isCompileSpjOk(Problem problem, String currentVersion) throws SystemError {
        CompileConfig compiler;
        String programFilePath;
        String programVersionPath;
        compiler = CompileConfig.getCompilerByLanguage("SPJ-" + problem.getSpjLanguage());

        programFilePath = JudgeDir.SPJ_WORKPLACE_DIR + File.separator + problem.getId() + File.separator
                + compiler.getExeName();

        programVersionPath = JudgeDir.SPJ_WORKPLACE_DIR + File.separator + problem.getId() + File.separator
                + "version";

        // 如果不存在该已经编译好的程序，则需要再次进行编译
        if (!FileUtil.exist(programFilePath) || !FileUtil.exist(programVersionPath)) {
            boolean isCompileSpjOk = Compiler.compileSpj(problem.getSpjCode(), problem.getId(),
                    problem.getSpjLanguage(), JudgeUtil.getProblemExtraFileMap(problem, "judge"));

            FileWriter fileWriter = new FileWriter(programVersionPath);
            fileWriter.write(currentVersion);
            return isCompileSpjOk;
        }

        FileReader spjVersionReader = new FileReader(programVersionPath);
        String recordSpjVersion = spjVersionReader.readString();

        // 版本变动也需要重新编译
        if (!currentVersion.equals(recordSpjVersion)) {
            boolean isCompileSpjOk = Compiler.compileSpj(problem.getSpjCode(), problem.getId(),
                    problem.getSpjLanguage(), JudgeUtil.getProblemExtraFileMap(problem, "judge"));
            FileWriter fileWriter = new FileWriter(programVersionPath);
            fileWriter.write(currentVersion);
            return isCompileSpjOk;
        }
        return null;
    }

    // 获取判题的运行时间，运行空间，OI得分
    public HashMap<String, Object> computeResultInfo(List<JudgeCase> allTestCaseResultList, Boolean isACM,
                                                     Integer errorCaseNum, Integer totalScore, Integer problemDifficulty) {
        HashMap<String, Object> result = new HashMap<>();
        // 用时和内存占用保存为多个测试点中最长的
        allTestCaseResultList.stream().max(Comparator.comparing(t -> t.getTime()))
                .ifPresent(t -> result.put("time", t.getTime()));

        allTestCaseResultList.stream().max(Comparator.comparing(t -> t.getMemory()))
                .ifPresent(t -> result.put("memory", t.getMemory()));

        // OI题目计算得分
        if (!isACM) {
            // 全对的直接用总分*0.1+2*题目难度
            if (errorCaseNum == 0) {
                int oiRankScore = (int) Math.round(totalScore * 0.1 + 2 * problemDifficulty);
                result.put("score", totalScore);
                result.put("oiRankScore", oiRankScore);
            } else {
                int sumScore = 0;
                for (JudgeCase testcaseResult : allTestCaseResultList) {
                    sumScore += testcaseResult.getScore();
                }
                // 测试点总得分*0.1+2*题目难度*（测试点总得分/题目总分）
                int oiRankScore = (int) Math
                        .round(sumScore * 0.1 + 2 * problemDifficulty * (sumScore * 1.0 / totalScore));
                result.put("score", sumScore);
                result.put("oiRankScore", oiRankScore);
            }
        }
        return result;
    }

    /**
     * 进行最终测试结果的判断（除编译失败外的评测状态码和时间，空间,OI题目的得分）
     * @param testCaseResultList
     * @param problem
     * @param judge
     * @return
     */
    public HashMap<String, Object> getJudgeInfo(List<JSONObject> testCaseResultList, Problem problem, Judge judge) {
        boolean isACM = problem.getType().equals(ContestEnum.TYPE_ACM.getCode());

        List<JSONObject> errorTestCaseList = new LinkedList<>();

        List<JudgeCase> allCaseResList = new LinkedList<>();

        // 记录所有测试点的结果
        testCaseResultList.forEach(jsonObject -> {
            Integer time = jsonObject.getLong("time").intValue();
            Integer memory = jsonObject.getLong("memory").intValue();
            Integer status = jsonObject.getInt("status");

            Long caseId = jsonObject.getLong("caseId", null);
            String inputFileName = jsonObject.getStr("inputFileName");
            String outputFileName = jsonObject.getStr("outputFileName");
            String msg = jsonObject.getStr("errMsg");
            JudgeCase judgeCase = new JudgeCase();
            judgeCase.setTime(time).setMemory(memory).setStatus(status).setInputData(inputFileName)
                    .setOutputData(outputFileName).setPid(problem.getId()).setUid(judge.getUid()).setCaseId(caseId)
                    .setSubmitId(judge.getSubmitId());

            if (!StrUtil.isEmpty(msg) && !status.equals(JudgeStatus.STATUS_COMPILE_ERROR.getStatus())) {
                judgeCase.setUserOutput(msg);
            }

            if (isACM) {
                if (!status.equals(JudgeStatus.STATUS_ACCEPTED.getStatus())) {
                    errorTestCaseList.add(jsonObject);
                }
            } else {
                int oiScore = jsonObject.getInt("score").intValue();
                if (status.equals(JudgeStatus.STATUS_ACCEPTED.getStatus())) {
                    judgeCase.setScore(oiScore);
                } else if (status.equals(JudgeStatus.STATUS_PARTIAL_ACCEPTED.getStatus())) {
                    errorTestCaseList.add(jsonObject);
                    Double percentage = jsonObject.getDouble("percentage");
                    if (percentage != null) {
                        int score = (int) Math.floor(percentage * oiScore);
                        judgeCase.setScore(score);
                    } else {
                        judgeCase.setScore(0);
                    }
                } else {
                    errorTestCaseList.add(jsonObject);
                    judgeCase.setScore(0);
                }
            }

            allCaseResList.add(judgeCase);
        });

        // 更新到数据库
        boolean addCaseRes = JudgeCaseEntityService.saveBatch(allCaseResList);
        if (!addCaseRes) {
            log.error("题号为：" + problem.getId() + "，提交id为：" + judge.getSubmitId() + "的各个测试数据点的结果更新到数据库操作失败");
        }

        // 获取判题的运行时间，运行空间，OI得分
        HashMap<String, Object> result = computeResultInfo(allCaseResList, isACM, errorTestCaseList.size(),
                problem.getIoScore(), problem.getDifficulty());

        // 如果该题为ACM类型的题目，多个测试点全部正确则AC，否则取第一个错误的测试点的状态
        // 如果该题为OI类型的题目, 若多个测试点全部正确则AC，若全部错误则取第一个错误测试点状态，否则为部分正确
        // 全部测试点正确，则为AC
        if (errorTestCaseList.size() == 0) {
            result.put("code", JudgeStatus.STATUS_ACCEPTED.getStatus());
        } else if (isACM || errorTestCaseList.size() == testCaseResultList.size()) {
            result.put("code", errorTestCaseList.get(0).getInt("status"));
            result.put("errMsg", errorTestCaseList.get(0).getStr("errMsg", ""));
        } else {
            result.put("code", JudgeStatus.STATUS_PARTIAL_ACCEPTED.getStatus());
        }
        return result;
    }

    private String getUserFileName(String language) {
        switch (language) {
            case "PHP":
                return "main.php";
            case "JavaScript Node":
            case "JavaScript V8":
                return "main.js";
        }
        return "main";
    }

    public String mergeNonEmptyStrings(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            if (!StrUtil.isEmpty(str)) {
                sb.append(str, 0, Math.min(1024 * 1024, str.length())).append("\n");
            }
        }
        return sb.toString();
    }

}
