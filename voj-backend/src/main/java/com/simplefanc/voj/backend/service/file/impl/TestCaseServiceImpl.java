package com.simplefanc.voj.backend.service.file.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusSystemErrorException;
import com.simplefanc.voj.backend.common.utils.MyFileUtil;
import com.simplefanc.voj.backend.dao.problem.ProblemCaseEntityService;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.service.file.TestCaseService;
import com.simplefanc.voj.common.pojo.entity.problem.ProblemCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:57
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final ProblemCaseEntityService problemCaseEntityService;

    private final FilePathProperties filePathProps;

    // TODO 行数过多
    @Override
    public Map<Object, Object> uploadTestcaseZip(MultipartFile file) {
        // 获取文件后缀
        String suffix = MyFileUtil.getFileSuffix(file);
        if (!"zip".toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请上传zip格式的测试数据压缩包！");
        }
        String fileDir = filePathProps.getTestcaseTmpFolder() + File.separator + IdUtil.simpleUUID();
        String filePath = fileDir + File.separator + file.getOriginalFilename();
        // 文件夹不存在就新建
        FileUtil.mkdir(fileDir);
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            log.error("评测数据文件上传异常-------------->", e);
            throw new StatusSystemErrorException("服务器异常：评测数据上传失败！");
        }

        // 将压缩包解压缩到指定文件夹
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

        HashMap<String, String> inputData = new HashMap<>();
        HashMap<String, String> outputData = new HashMap<>();

        // 遍历读取与检查是否in和out文件一一对应，否则报错
        for (File tmp : files) {
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

        // 进行数据对应检查,同时生成返回数据
        List<HashMap<String, String>> problemCaseList = new LinkedList<>();
        for (String key : inputData.keySet()) {
            HashMap<String, String> testcaseMap = new HashMap<>();
            String inputFileName = inputData.get(key);
            testcaseMap.put("input", inputFileName);

            // 若有名字对应的out文件不存在的，直接生成对应的out文件
            final String outputFileName = outputData.getOrDefault(key, null);
            if (outputFileName == null) {
                FileWriter fileWriter = new FileWriter(fileDir + File.separator + key + ".out");
                fileWriter.write("");
            }

            testcaseMap.put("output", outputFileName);
            problemCaseList.add(testcaseMap);
        }

        List<HashMap<String, String>> fileList = problemCaseList.stream().sorted((o1, o2) -> {
            String a = o1.get("input").split("\\.")[0];
            String b = o2.get("input").split("\\.")[0];
            if (a.length() > b.length()) {
                return 1;
            } else if (a.length() < b.length()) {
                return -1;
            }
            return a.compareTo(b);
        }).collect(Collectors.toList());

        return MapUtil.builder().put("fileList", fileList).put("fileListDir", fileDir).map();
    }

    @Override
    public void downloadTestcase(Long pid, HttpServletResponse response) {
        String workDir = filePathProps.getTestcaseBaseFolder() + File.separator + "problem_" + pid;
        File file = new File(workDir);
        // 本地为空 尝试去数据库查找
        if (!file.exists()) {
            QueryWrapper<ProblemCase> problemCaseQueryWrapper = new QueryWrapper<>();
            problemCaseQueryWrapper.eq("pid", pid);
            List<ProblemCase> problemCaseList = problemCaseEntityService.list(problemCaseQueryWrapper);

            if (CollectionUtils.isEmpty(problemCaseList)) {
                throw new StatusFailException("对不起，该题目的评测数据为空！");
            }

            boolean hasTestCase = true;
            if (problemCaseList.get(0).getInput().endsWith(".in")
                    && (problemCaseList.get(0).getOutput().endsWith(".out")
                    || problemCaseList.get(0).getOutput().endsWith(".ans"))) {
                hasTestCase = false;
            }
            if (!hasTestCase) {
                throw new StatusFailException("对不起，该题目的评测数据为空！");
            }

            // 为手动输入的测试用例
            FileUtil.mkdir(workDir);
            // 写入本地
            for (int i = 0; i < problemCaseList.size(); i++) {
                String filePreName = workDir + File.separator + (i + 1);
                String inputName = filePreName + ".in";
                String outputName = filePreName + ".out";
                FileWriter infileWriter = new FileWriter(inputName);
                infileWriter.write(problemCaseList.get(i).getInput());
                FileWriter outfileWriter = new FileWriter(outputName);
                outfileWriter.write(problemCaseList.get(i).getOutput());
            }
        }

        String fileName = "problem_" + pid + "_testcase_" + System.currentTimeMillis() + ".zip";
        // 将对应文件夹的文件压缩成zip
        final String zipPath = filePathProps.getFileDownloadTmpFolder() + File.separator + fileName;
        ZipUtil.zip(workDir, zipPath);
        MyFileUtil.download(response, zipPath, fileName, "下载题目测试数据的压缩文件失败，请重新尝试！");
        // 清空临时文件
        FileUtil.del(zipPath);
    }

    @Override
    public void downloadSingleTestCase(Long pid, String inputData, String outputData, HttpServletResponse response) {
        String workDir = filePathProps.getTestcaseBaseFolder() + File.separator + "problem_" + pid;
        String fileName = "problem_" + pid + "_testcase_" + System.currentTimeMillis() + ".zip";
        // 将对应文件夹的文件压缩成zip
        final String zipPath = filePathProps.getFileDownloadTmpFolder() + File.separator + fileName;

        ZipUtil.zip(FileUtil.file(zipPath), false,
                FileUtil.file(workDir + File.separator + inputData),
                FileUtil.file(workDir + File.separator + outputData)
        );

        MyFileUtil.download(response, zipPath, fileName, "下载题目测试数据的压缩文件失败，请重新尝试！");
        // 清空临时文件
        FileUtil.del(zipPath);
    }

}