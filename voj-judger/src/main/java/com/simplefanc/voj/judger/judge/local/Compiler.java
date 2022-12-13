package com.simplefanc.voj.judger.judge.local;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.judger.common.constants.CompileConfig;
import com.simplefanc.voj.judger.common.constants.JudgeDir;
import com.simplefanc.voj.judger.common.exception.CompileException;
import com.simplefanc.voj.judger.common.exception.SubmitException;
import com.simplefanc.voj.judger.common.exception.SystemException;
import com.simplefanc.voj.judger.common.utils.JudgeUtil;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2021/4/16 12:14
 * @Description: 判题流程解耦重构2.0，该类只负责编译
 */
public class Compiler {

    public static String compile(CompileConfig compileConfig, String code, String language,
                                 HashMap<String, String> extraFiles) throws SystemException, CompileException, SubmitException {

        if (compileConfig == null) {
            throw new RuntimeException("Unsupported language " + language);
        }

        // 调用安全沙箱进行编译
        JSONArray result = SandboxRun.compile(compileConfig.getMaxCpuTime(), compileConfig.getMaxRealTime(),
                compileConfig.getMaxMemory(), 256 * 1024 * 1024L, compileConfig.getSrcName(),
                compileConfig.getExeName(), parseCompileCommand(compileConfig.getCommand(), compileConfig),
                compileConfig.getEnvs(), code, extraFiles, true, false, null);
        JSONObject compileResult = (JSONObject) result.get(0);
        if (compileResult.getInt("status").intValue() != JudgeStatus.STATUS_ACCEPTED.getStatus()) {
            throw new CompileException("Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }

        String fileId = ((JSONObject) compileResult.get("fileIds")).getStr(compileConfig.getExeName());
        if (StrUtil.isEmpty(fileId)) {
            throw new SubmitException("Executable file not found.",
                    ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }
        return fileId;
    }

    public static Boolean compileSpj(String code, Long pid, String language, HashMap<String, String> extraFiles)
            throws SystemException {

        CompileConfig spjCompiler = CompileConfig.getCompilerByLanguage("SPJ-" + language);
        if (spjCompiler == null) {
            throw new RuntimeException("Unsupported SPJ language:" + language);
        }

        boolean copyOutExe = true;
        // 题目id为空，则不进行本地存储，可能为新建题目时测试特判程序是否正常的判断而已
        if (pid == null) {
            copyOutExe = false;
        }

        // 调用安全沙箱对特别判题程序进行编译
        JSONArray res = SandboxRun.compile(spjCompiler.getMaxCpuTime(), spjCompiler.getMaxRealTime(),
                spjCompiler.getMaxMemory(), 256 * 1024 * 1024L, spjCompiler.getSrcName(), spjCompiler.getExeName(),
                parseCompileCommand(spjCompiler.getCommand(), spjCompiler), spjCompiler.getEnvs(), code, extraFiles,
                false, copyOutExe, JudgeDir.SPJ_WORKPLACE_DIR + File.separator + pid);
        JSONObject compileResult = (JSONObject) res.get(0);
        if (compileResult.getInt("status").intValue() != JudgeStatus.STATUS_ACCEPTED.getStatus()) {
            throw new SystemException("Special Judge Code Compile Error.",
                    ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }
        return true;
    }

    public static Boolean compileInteractive(String code, Long pid, String language, HashMap<String, String> extraFiles)
            throws SystemException {

        CompileConfig interactiveCompiler = CompileConfig.getCompilerByLanguage("INTERACTIVE-" + language);
        if (interactiveCompiler == null) {
            throw new RuntimeException("Unsupported interactive language:" + language);
        }

        // 题目id为空，则不进行本地存储，可能为新建题目时测试特判程序是否正常的判断而已
        boolean copyOutExe = pid != null;

        // 调用安全沙箱对特别判题程序进行编译
        JSONArray res = SandboxRun.compile(interactiveCompiler.getMaxCpuTime(), interactiveCompiler.getMaxRealTime(),
                interactiveCompiler.getMaxMemory(), 256 * 1024 * 1024L, interactiveCompiler.getSrcName(),
                interactiveCompiler.getExeName(),
                parseCompileCommand(interactiveCompiler.getCommand(), interactiveCompiler),
                interactiveCompiler.getEnvs(), code, extraFiles, false, copyOutExe,
                JudgeDir.INTERACTIVE_WORKPLACE_DIR + File.separator + pid);
        JSONObject compileResult = (JSONObject) res.get(0);
        if (compileResult.getInt("status").intValue() != JudgeStatus.STATUS_ACCEPTED.getStatus()) {
            throw new SystemException("Interactive Judge Code Compile Error.",
                    ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }
        return true;
    }

    private static List<String> parseCompileCommand(String command, CompileConfig compileConfig) {

        command = MessageFormat.format(command, JudgeDir.TMPFS_DIR, compileConfig.getSrcName(),
                compileConfig.getExeName());
        return JudgeUtil.translateCommandline(command);
    }

}