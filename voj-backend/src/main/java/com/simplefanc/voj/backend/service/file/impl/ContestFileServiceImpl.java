package com.simplefanc.voj.backend.service.file.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.utils.MyFileUtil;
import com.simplefanc.voj.backend.common.utils.ExcelUtil;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestPrintEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestProblemEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.pojo.vo.ACMContestRankVO;
import com.simplefanc.voj.backend.pojo.vo.ExcelIpVO;
import com.simplefanc.voj.backend.pojo.vo.OIContestRankVO;
import com.simplefanc.voj.backend.service.file.ContestFileService;
import com.simplefanc.voj.backend.service.oj.ContestACMRankService;
import com.simplefanc.voj.backend.service.oj.ContestOIRankService;
import com.simplefanc.voj.backend.service.oj.ContestService;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestPrint;
import com.simplefanc.voj.common.pojo.entity.contest.ContestProblem;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:27
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class ContestFileServiceImpl implements ContestFileService {

    private static final ThreadLocal<SimpleDateFormat> THREAD_LOCAL_TIME = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmss"));

    private final ContestEntityService contestEntityService;

    private final ContestProblemEntityService contestProblemEntityService;

    private final ContestPrintEntityService contestPrintEntityService;

    private final ContestService contestService;

    private final JudgeEntityService judgeEntityService;

    private final UserInfoEntityService userInfoEntityService;

    private final ContestACMRankService contestACMRankService;

    private final ContestOIRankService contestOIRankService;

    private final ContestValidator contestValidator;

    private final FilePathProperties filePathProps;

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private static String languageToFileSuffix(String language) {

        List<String> CLang = Arrays.asList("c", "gcc", "clang");
        List<String> CPPLang = Arrays.asList("c++", "g++", "clang++");
        List<String> PythonLang = Arrays.asList("python", "pypy");

        for (String lang : CPPLang) {
            if (language.contains(lang)) {
                return "cpp";
            }
        }

        if (language.contains("c#")) {
            return "cs";
        }

        for (String lang : CLang) {
            if (language.contains(lang)) {
                return "c";
            }
        }

        for (String lang : PythonLang) {
            if (language.contains(lang)) {
                return "py";
            }
        }

        if (language.contains("javascript")) {
            return "js";
        }

        if (language.contains("java")) {
            return "java";
        }

        if (language.contains("pascal")) {
            return "pas";
        }

        if (language.contains("go")) {
            return "go";
        }

        if (language.contains("php")) {
            return "php";
        }

        return "txt";
    }

    @Override
    public void downloadContestRank(Long cid, Boolean forceRefresh, Boolean removeStar, HttpServletResponse response)
            throws IOException {
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("错误：该比赛不存在！");
        }

        if (!contestValidator.isContestAdmin(contest)) {
            throw new StatusForbiddenException("错误：您并非该比赛的管理员，无权下载榜单！");
        }

        // 检查是否开启封榜模式
        boolean isOpenSealRank = contestValidator.isOpenSealRank(contest, forceRefresh);

        // 获取题目displayID列表
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", contest.getId()).select("display_id").orderByAsc("display_id");
        List<String> contestProblemDisplayIdList = contestProblemEntityService.list(contestProblemQueryWrapper)
                .stream()
                .sorted()
                .map(ContestProblem::getDisplayId)
                .collect(Collectors.toList());

        List<List<String>> head;
        List data;
        // ACM比赛
        if (contest.getType().intValue() == ContestEnum.TYPE_ACM.getCode()) {
            List<ACMContestRankVO> acmContestRankVOList = contestACMRankService.calculateACMRank(isOpenSealRank, removeStar, contest,
                    null, null, false, null);
            head = getContestRankExcelHead(contestProblemDisplayIdList, true);
            data = changeACMContestRankToExcelRowList(acmContestRankVOList,
                            contestProblemDisplayIdList, contest.getRankShowName());
        } else {
            List<OIContestRankVO> oiContestRankVOList = contestOIRankService.calculateOIRank(isOpenSealRank,
                    removeStar, contest, null, null, false, null);
            head = getContestRankExcelHead(contestProblemDisplayIdList, false);
            data = changeOIContestRankToExcelRowList(oiContestRankVOList, contestProblemDisplayIdList, contest.getRankShowName());
        }

        final String fileName = "contest_" + contest.getId() + "_rank";
        ExcelUtil.wrapExcelResponse(response, fileName);
        final ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet rankSheet = EasyExcel.writerSheet(0, "rank").head(head).build();
        WriteSheet ipSheet = EasyExcel.writerSheet(1, "ip").head(ExcelIpVO.class).build();
        excelWriter.write(data, rankSheet)
                .write(getExcelIpVO(contest), ipSheet);
        excelWriter.finish();
    }

    private List<ExcelIpVO> getExcelIpVO(Contest contest) {
        final Set<String> contestAdminUidList = contestService.getContestAdminUidList(contest);
        final List<Judge> judgeList = judgeEntityService.list(new QueryWrapper<Judge>().select("DISTINCT username, ip")
                .eq("cid", contest.getId())
                .between("submit_time", contest.getStartTime(), contest.getEndTime()));

        final Map<String, String> userNameIpMap = judgeList.stream()
                .filter(judge -> !contestAdminUidList.contains(judge.getUid()))
                .collect(Collectors.groupingBy(Judge::getUsername,
                        Collectors.mapping(Judge::getIp,
                                Collectors.joining(" & "))));
        return userNameIpMap.entrySet().stream()
                .map(entry -> new ExcelIpVO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ExcelIpVO::getUsername))
                .collect(Collectors.toList());
    }

    @Override
    public void downloadContestAcSubmission(Long cid, Boolean excludeAdmin, String splitType,
                                            HttpServletResponse response) {

        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("错误：该比赛不存在！");
        }

        if (!contestValidator.isContestAdmin(contest)) {
            throw new StatusForbiddenException("错误：您并非该比赛的管理员，无权下载AC记录！");
        }

        boolean isACM = contest.getType().intValue() == ContestEnum.TYPE_ACM.getCode();

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", contest.getId());
        List<ContestProblem> contestProblemList = contestProblemEntityService.list(contestProblemQueryWrapper);

        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();

        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.eq("cid", cid).eq(isACM, "status", JudgeStatus.STATUS_ACCEPTED.getStatus())
                // OI模式取得分不为null的
                .isNotNull(!isACM, "score").between("submit_time", contest.getStartTime(), contest.getEndTime())
                // 排除比赛创建者和root
                .ne(excludeAdmin, "uid", contest.getUid())
                .notIn(excludeAdmin && superAdminUidList.size() > 0, "uid", superAdminUidList)
                .orderByDesc("submit_time");

        List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);

        // 打包文件的临时路径 -> username为文件夹名字
        String tmpFilesDir = filePathProps.getContestAcSubmissionTmpFolder() + File.separator + IdUtil.fastSimpleUUID();
        FileUtil.mkdir(tmpFilesDir);

        HashMap<String, Boolean> recordMap = new HashMap<>();
        if ("user".equals(splitType)) {
            splitCodeByUser(isACM, contestProblemList, judgeList, tmpFilesDir, recordMap);
        } else if ("problem".equals(splitType)) {
            splitByProblem(isACM, contestProblemList, judgeList, tmpFilesDir, recordMap);
        }

        String zipFileName = "contest_" + contest.getId() + "_" + System.currentTimeMillis() + ".zip";
        String zipPath = filePathProps.getContestAcSubmissionTmpFolder() + File.separator + zipFileName;
        ZipUtil.zip(tmpFilesDir, zipPath);
        MyFileUtil.download(response, zipPath, zipFileName, "下载比赛AC代码失败，请重新尝试！");
        FileUtil.del(tmpFilesDir);
        FileUtil.del(zipPath);

    }

    /**
     * 以比赛题目编号来分割提交的代码
     */
    private void splitByProblem(boolean isACM, List<ContestProblem> contestProblemList, List<Judge> judgeList, String tmpFilesDir, HashMap<String, Boolean> recordMap) {
        for (ContestProblem contestProblem : contestProblemList) {
            // 对于每题目生成对应的文件夹
            String problemDir = tmpFilesDir + File.separator + contestProblem.getDisplayId();
            FileUtil.mkdir(problemDir);
            // 如果是ACM模式，则所有提交代码都要生成，如果同一题多次提交AC，加上提交时间秒后缀 ---> username_(666666).c
            // 如果是OI模式就生成最近一次提交即可，且带上分数 ---> username_(666666)_100.c
            List<Judge> problemSubmissionList = judgeList.stream()
                    // 过滤出对应题目的提交
                    .filter(judge -> judge.getPid().equals(contestProblem.getPid()))
                    // 根据提交时间进行降序
                    .sorted(Comparator.comparing(Judge::getSubmitTime).reversed()).collect(Collectors.toList());

            for (Judge judge : problemSubmissionList) {
                String filePath = problemDir + File.separator + judge.getUsername();
                if (!isACM) {
                    String key = judge.getUsername() + "_" + contestProblem.getDisplayId();
                    // OI模式只取最后一次提交
                    if (!recordMap.containsKey(key)) {
                        filePath += "_" + judge.getScore() + "_("
                                + THREAD_LOCAL_TIME.get().format(judge.getSubmitTime()) + ")."
                                + languageToFileSuffix(judge.getLanguage().toLowerCase());
                        FileWriter fileWriter = new FileWriter(filePath);
                        fileWriter.write(judge.getCode());
                        recordMap.put(key, true);
                    }
                } else {
                    filePath += "_(" + THREAD_LOCAL_TIME.get().format(judge.getSubmitTime()) + ")."
                            + languageToFileSuffix(judge.getLanguage().toLowerCase());
                    FileWriter fileWriter = new FileWriter(filePath);
                    fileWriter.write(judge.getCode());
                }
            }
        }
    }

    /**
     * 以用户来分割提交的代码
     */
    private void splitCodeByUser(boolean isACM, List<ContestProblem> contestProblemList, List<Judge> judgeList, String tmpFilesDir, HashMap<String, Boolean> recordMap) {
        List<String> usernameList = judgeList.stream()
                // 根据用户名过滤唯一
                .filter(distinctByKey(Judge::getUsername))
                // 映射出用户名列表
                .map(Judge::getUsername)
                .collect(Collectors.toList());

        HashMap<Long, String> cpIdMap = new HashMap<>();
        for (ContestProblem contestProblem : contestProblemList) {
            cpIdMap.put(contestProblem.getId(), contestProblem.getDisplayId());
        }

        for (String username : usernameList) {
            // 对于每个用户生成对应的文件夹
            String userDir = tmpFilesDir + File.separator + username;
            FileUtil.mkdir(userDir);
            // 如果是ACM模式，则所有提交代码都要生成，如果同一题多次提交AC，加上提交时间秒后缀 ---> A_(666666).c
            // 如果是OI模式就生成最近一次提交即可，且带上分数 ---> A_(666666)_100.c
            List<Judge> userSubmissionList = judgeList.stream()
                    // 过滤出对应用户的提交
                    .filter(judge -> judge.getUsername().equals(username))
                    // 根据提交时间进行降序
                    .sorted(Comparator.comparing(Judge::getSubmitTime).reversed()).collect(Collectors.toList());

            for (Judge judge : userSubmissionList) {
                String filePath = userDir + File.separator + cpIdMap.getOrDefault(judge.getCpid(), "null");

                // OI模式只取最后一次提交
                if (!isACM) {
                    String key = judge.getUsername() + "_" + judge.getPid();
                    if (!recordMap.containsKey(key)) {
                        filePath += "_" + judge.getScore() + "_("
                                + THREAD_LOCAL_TIME.get().format(judge.getSubmitTime()) + ")."
                                + languageToFileSuffix(judge.getLanguage().toLowerCase());
                        FileWriter fileWriter = new FileWriter(filePath);
                        fileWriter.write(judge.getCode());
                        recordMap.put(key, true);
                    }

                } else {
                    filePath += "_(" + THREAD_LOCAL_TIME.get().format(judge.getSubmitTime()) + ")."
                            + languageToFileSuffix(judge.getLanguage().toLowerCase());
                    FileWriter fileWriter = new FileWriter(filePath);
                    fileWriter.write(judge.getCode());
                }

            }
        }
    }

    @Override
    public void downloadContestPrintText(Long id, HttpServletResponse response) {
        ContestPrint contestPrint = contestPrintEntityService.getById(id);
        String filename = contestPrint.getUsername() + "_Contest_Print.txt";
        String filePath = filePathProps.getContestTextPrintFolder() + File.separator + id + File.separator + filename;
        if (!FileUtil.exist(filePath)) {
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(contestPrint.getContent());
        }

        MyFileUtil.download(response, filePath, filename, "下载比赛打印文本文件失败，请重新尝试！");
    }

    public List<List<String>> getContestRankExcelHead(List<String> contestProblemDisplayIdList, Boolean isACM) {
        List<List<String>> headList = new LinkedList<>();
        List<String> head = new LinkedList<>();
        head.add("No");

        List<String> head0 = new LinkedList<>();
        head0.add("Rank");

        List<String> head1 = new LinkedList<>();
        head1.add("Username");
//        List<String> head2 = new LinkedList<>();
//        head2.add("ShowName");
        List<String> head3 = new LinkedList<>();
        head3.add("Real Name");
        List<String> head4 = new LinkedList<>();
        head4.add("School");

        headList.add(head);
        headList.add(head0);
        headList.add(head1);
//        headList.add(head2);
        headList.add(head3);
        headList.add(head4);

        List<String> head5 = new LinkedList<>();
        if (isACM) {
            head5.add("AC");
            List<String> head6 = new LinkedList<>();
            head6.add("Total Submission");
            List<String> head7 = new LinkedList<>();
            head7.add("Total Penalty Time");
            headList.add(head5);
            headList.add(head6);
            headList.add(head7);
        } else {
            head5.add("Total Score");
            headList.add(head5);
        }

        // 添加题目头
        for (String displayId : contestProblemDisplayIdList) {
            List<String> tmp = new LinkedList<>();
            tmp.add(displayId);
            headList.add(tmp);
        }
        return headList;
    }

    public List<List<Object>> changeACMContestRankToExcelRowList(List<ACMContestRankVO> acmContestRankVOList,
                                                                 List<String> contestProblemDisplayIdList, String rankShowName) {
        List<List<Object>> allRowDataList = new LinkedList<>();
        for (ACMContestRankVO acmContestRankVO : acmContestRankVOList) {
            List<Object> rowData = new LinkedList<>();
            rowData.add(acmContestRankVO.getSeq());
            rowData.add(acmContestRankVO.getRank() == -1 ? "*" : acmContestRankVO.getRank().toString());
            rowData.add(acmContestRankVO.getUsername());
//            if ("username".equals(rankShowName)) {
//                rowData.add(acmContestRankVO.getUsername());
//            } else if ("realname".equals(rankShowName)) {
//                rowData.add(acmContestRankVO.getRealname());
//            } else if ("nickname".equals(rankShowName)) {
//                rowData.add(acmContestRankVO.getNickname());
//            } else {
//                rowData.add("");
//            }
            rowData.add(acmContestRankVO.getRealname());
            rowData.add(acmContestRankVO.getSchool());
            rowData.add(acmContestRankVO.getAc());
            rowData.add(acmContestRankVO.getTotal());
            rowData.add(acmContestRankVO.getTotalTime());
            HashMap<String, HashMap<String, Object>> submissionInfo = acmContestRankVO.getSubmissionInfo();
            for (String displayId : contestProblemDisplayIdList) {
                HashMap<String, Object> problemInfo = submissionInfo.getOrDefault(displayId, null);
                // 如果是有提交记录的
                if (problemInfo != null) {
                    boolean isAC = (boolean) problemInfo.getOrDefault("isAC", false);
                    String info = "";
                    int errorNum = (int) problemInfo.getOrDefault("errorNum", 0);
                    int tryNum = (int) problemInfo.getOrDefault("tryNum", 0);
                    if (isAC) {
                        if (errorNum == 0) {
                            info = "+(1)";
                        } else {
                            info = "-(" + (errorNum + 1) + ")";
                        }
                    } else {
                        if (tryNum != 0 && errorNum != 0) {
                            info = "-(" + errorNum + "+" + tryNum + ")";
                        } else if (errorNum != 0) {
                            info = "-(" + errorNum + ")";
                        } else if (tryNum != 0) {
                            info = "?(" + tryNum + ")";
                        }
                    }
                    rowData.add(info);
                } else {
                    rowData.add("");
                }
            }
            allRowDataList.add(rowData);
        }
        return allRowDataList;
    }

    public List<List<Object>> changeOIContestRankToExcelRowList(List<OIContestRankVO> oiContestRankVOList,
                                                                List<String> contestProblemDisplayIdList, String rankShowName) {
        List<List<Object>> allRowDataList = new LinkedList<>();
        for (OIContestRankVO oiContestRankVO : oiContestRankVOList) {
            List<Object> rowData = new LinkedList<>();
            rowData.add(oiContestRankVO.getSeq());
            rowData.add(oiContestRankVO.getRank() == -1 ? "*" : oiContestRankVO.getRank().toString());
            rowData.add(oiContestRankVO.getUsername());
//            if ("username".equals(rankShowName)) {
//                rowData.add(oiContestRankVO.getUsername());
//            } else if ("realname".equals(rankShowName)) {
//                rowData.add(oiContestRankVO.getRealname());
//            } else if ("nickname".equals(rankShowName)) {
//                rowData.add(oiContestRankVO.getNickname());
//            } else {
//                rowData.add("");
//            }
            rowData.add(oiContestRankVO.getRealname());
            rowData.add(oiContestRankVO.getSchool());
            rowData.add(oiContestRankVO.getTotalScore());
            Map<String, Integer> submissionInfo = oiContestRankVO.getSubmissionInfo();
            for (String displayId : contestProblemDisplayIdList) {
                Integer score = submissionInfo.getOrDefault(displayId, null);
                // 如果是有提交记录的就写最后一次提交的分数，没有的就写空
                if (score != null) {
                    rowData.add(score);
                } else {
                    rowData.add("");
                }
            }
            allRowDataList.add(rowData);
        }
        return allRowDataList;
    }

}