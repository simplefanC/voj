package com.simplefanc.voj.judger.judge.local;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.simplefanc.voj.judger.common.constants.JudgeDir;
import com.simplefanc.voj.judger.common.constants.RunConfig;
import com.simplefanc.voj.judger.common.exception.SystemException;
import com.simplefanc.voj.judger.common.utils.JudgeUtil;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeDTO;
import com.simplefanc.voj.judger.judge.local.pojo.JudgeGlobalDTO;
import com.simplefanc.voj.judger.judge.local.pojo.SandBoxRes;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/1/2 20:46
 * @Description:
 */
public abstract class AbstractJudge {

    protected static final int SPJ_PC = 99;

    protected static final int SPJ_AC = 100;

    protected static final int SPJ_PE = 101;

    protected static final int SPJ_WA = 102;

    protected static final int SPJ_ERROR = 103;

    public JSONObject judge(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemException {
        // 判题
        JSONArray judgeResultList = judgeCase(judgeDTO, judgeGlobalDTO);

        // 处理判题结果
        switch (judgeGlobalDTO.getJudgeMode()) {
            case SPJ:
            case DEFAULT:
                return handle(judgeDTO, judgeGlobalDTO, judgeResultList);
            case INTERACTIVE:
                return handleMultiple(judgeDTO, judgeGlobalDTO, judgeResultList);
            default:
                throw new RuntimeException("The problem mode is error:" + judgeGlobalDTO.getJudgeMode());
        }

    }

    public abstract JSONArray judgeCase(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemException;

    public abstract JSONObject processResult(SandBoxRes sandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO)
            throws SystemException;

    public abstract JSONObject processMultipleResult(SandBoxRes userSandBoxRes, SandBoxRes interactiveSandBoxRes,
                                                     JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO);


    private SandBoxRes wrapToSandBoxRes(JSONObject judgeResult) {
        return SandBoxRes.builder()
                // 普通评测：如果沙盒运行程序不是 Accepted 可以不获取 stdout
                .stdout(((JSONObject) judgeResult.get("files")).getStr("stdout"))
                .stderr(((JSONObject) judgeResult.get("files")).getStr("stderr"))
                // ns->ms
                .time(judgeResult.getLong("time") / 1_000_000)
                // b-->kb
                .memory(judgeResult.getLong("memory") / 1024)
                .exitCode(judgeResult.getInt("exitStatus"))
                .status(judgeResult.getInt("status"))
                .build();
    }

    private JSONObject handle(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO, JSONArray judgeResultList)
            throws SystemException {
        SandBoxRes sandBoxRes = wrapToSandBoxRes((JSONObject) judgeResultList.get(0));
        return processResult(sandBoxRes, judgeDTO, judgeGlobalDTO);
    }

    private JSONObject handleMultiple(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO, JSONArray judgeResultList) {
        JSONObject userJudgeResult = (JSONObject) judgeResultList.get(0);
        SandBoxRes userSandBoxRes = wrapToSandBoxRes(userJudgeResult);

        JSONObject interactiveJudgeResult = (JSONObject) judgeResultList.get(1);
        SandBoxRes interactiveSandBoxRes = wrapToSandBoxRes(interactiveJudgeResult);
        return processMultipleResult(userSandBoxRes, interactiveSandBoxRes, judgeDTO, judgeGlobalDTO);
    }

    protected List<String> parseRunCommand(RunConfig runConfig, String testCaseInputName,
                                           String userOutputName, String testCaseOutputName) {
        String command = runConfig.getCommand();
        command = MessageFormat.format(command, JudgeDir.TMPFS_DIR, runConfig.getExeName(),
                JudgeDir.TMPFS_DIR + File.separator + testCaseInputName,
                JudgeDir.TMPFS_DIR + File.separator + userOutputName,
                JudgeDir.TMPFS_DIR + File.separator + testCaseOutputName);

        return JudgeUtil.translateCommandline(command);
    }

    protected JSONObject parseTestLibErr(String msg) {

        JSONObject res = new JSONObject(2);
        String output = msg.substring(0, Math.min(1024, msg.length()));
        if (output.startsWith("ok ")) {
            res.set("code", SPJ_AC);
            res.set("errMsg", output.split("ok ")[1]);
        } else if (output.startsWith("wrong answer ")) {
            res.set("code", SPJ_WA);
            res.set("errMsg", output.split("wrong answer ")[1]);
        } else if (output.startsWith("wrong output format ")) {
            res.set("code", SPJ_WA);
            res.set("errMsg", "May be output presentation error. " + output.split("wrong output format")[1]);
        } else if (output.startsWith("partially correct ")) {
            res.set("errMsg", output.split("partially correct ")[1]);
            String numStr = ReUtil.get("partially correct \\(([\\s\\S]*?)\\) ", output, 1);
            double percentage = 0.0;
            if (!StrUtil.isEmpty(numStr)) {
                percentage = Integer.parseInt(numStr) * 1.0 / 100;
            }
            res.set("percentage", percentage);
            res.set("code", SPJ_PC);
        } else if (output.startsWith("points ")) {
            res.set("code", SPJ_PC);
            String numStr = output.split("points ")[1].split(" ")[0];
            double percentage = 0.0;
            if (!StrUtil.isEmpty(numStr)) {
                percentage = Double.parseDouble(numStr) / 100;
            }
            if (percentage == 1) {
                res.set("code", SPJ_AC);
            } else {
                res.set("percentage", percentage);
            }
            String tmp = output.split("points ")[1];
            res.set("errMsg", tmp.substring(0, Math.min(1024, tmp.length())));
        } else if (output.startsWith("FAIL ")) {
            res.set("code", SPJ_ERROR);
            res.set("errMsg", output.split("FAIL ")[1]);
        } else {
            res.set("code", SPJ_ERROR);
            res.set("errMsg", output);
        }
        return res;
    }

}