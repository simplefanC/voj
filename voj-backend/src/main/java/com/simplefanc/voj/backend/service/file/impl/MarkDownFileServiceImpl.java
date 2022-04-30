package com.simplefanc.voj.backend.service.file.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.exception.StatusSystemErrorException;
import com.simplefanc.voj.backend.dao.common.FileEntityService;
import com.simplefanc.voj.backend.pojo.bo.FilePathProps;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.file.MarkDownFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:50
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
public class MarkDownFileServiceImpl implements MarkDownFileService {

    @Resource
    private FileEntityService fileEntityService;

    @Autowired
    private FilePathProps filePathProps;

    @Override
    public Map<Object, Object> uploadMDImg(MultipartFile image) {
        if (image == null) {
            throw new StatusFailException("上传的图片不能为空！");
        }
        if (image.getSize() > 1024 * 1024 * 4) {
            throw new StatusFailException("上传的图片文件大小不能大于4M！");
        }
        //获取文件后缀
        String suffix = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"jpg,jpeg,gif,png,webp".toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请选择jpg,jpeg,gif,png,webp格式的图片！");
        }

        //若不存在该目录，则创建目录
        FileUtil.mkdir(filePathProps.getMarkdownFileFolder());

        //通过UUID生成唯一文件名
        String filename = IdUtil.simpleUUID() + "." + suffix;
        try {
            //将文件保存指定目录
            image.transferTo(FileUtil.file(filePathProps.getMarkdownFileFolder() + File.separator + filename));
        } catch (Exception e) {
            log.error("图片文件上传异常-------------->", e);
            throw new StatusSystemErrorException("服务器异常：图片文件上传失败！");
        }

        // 获取当前登录用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        com.simplefanc.voj.common.pojo.entity.common.File file = new com.simplefanc.voj.common.pojo.entity.common.File();
        file.setFolderPath(filePathProps.getMarkdownFileFolder())
                .setName(filename)
                .setFilePath(filePathProps.getMarkdownFileFolder() + File.separator + filename)
                .setSuffix(suffix)
                .setType("md")
                .setUid(userRolesVo.getUid());
        fileEntityService.save(file);

        return MapUtil.builder()
                .put("link", filePathProps.getImgApi() + filename)
                .put("fileId", file.getId()).map();

    }


    @Override
    public void deleteMDImg(Long fileId) {

        // 获取当前登录用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        com.simplefanc.voj.common.pojo.entity.common.File file = fileEntityService.getById(fileId);

        if (file == null) {
            throw new StatusFailException("错误：文件不存在！");
        }

        if (!"md".equals(file.getType())) {
            throw new StatusForbiddenException("错误：不支持删除！");
        }

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");
        boolean isAdmin = SecurityUtils.getSubject().hasRole("admin");

        if (!file.getUid().equals(userRolesVo.getUid()) && !isRoot && !isAdmin && !isProblemAdmin) {
            throw new StatusForbiddenException("错误：无权删除他人文件！");
        }

        boolean isOk = FileUtil.del(file.getFilePath());
        if (isOk) {
            fileEntityService.removeById(fileId);
        } else {
            throw new StatusFailException("删除失败");
        }

    }


    @Override
    public Map<Object, Object> uploadMd(MultipartFile file) {
        if (file == null) {
            throw new StatusFailException("上传的文件不能为空！");
        }
        if (file.getSize() >= 1024 * 1024 * 128) {
            throw new StatusFailException("上传的文件大小不能大于128M！");
        }
        // 获取文件后缀
        String suffix = "";
        String filename = "";
        if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
            suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            // 通过UUID生成唯一文件名
            filename = IdUtil.simpleUUID() + "." + suffix;
        } else {
            filename = IdUtil.simpleUUID();
        }
        // 若不存在该目录，则创建目录
        FileUtil.mkdir(filePathProps.getMarkdownFileFolder());

        try {
            // 将文件保存指定目录
            file.transferTo(FileUtil.file(filePathProps.getMarkdownFileFolder() + File.separator + filename));
        } catch (Exception e) {
            log.error("文件上传异常-------------->", e);
            throw new StatusSystemErrorException("服务器异常：文件上传失败！");
        }

        return MapUtil.builder()
                .put("link", filePathProps.getFileApi() + filename)
                .map();
    }

}