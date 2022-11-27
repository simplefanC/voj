package com.simplefanc.voj.backend.service.file.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.backend.common.constants.FileTypeEnum;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusSystemErrorException;
import com.simplefanc.voj.backend.dao.common.FileEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.config.property.FilePathProperties;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.service.file.ImageService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.user.Role;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:31
 * @Description:
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    public static final String IMAGE_FORMAT = "jpg,jpeg,gif,png,webp,jfif,svg";

    private final FileEntityService fileEntityService;

    private final UserInfoEntityService userInfoEntityService;

    private final FilePathProperties filePathProps;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Object, Object> uploadAvatar(MultipartFile image) {
        if (image == null) {
            throw new StatusFailException("上传的头像图片文件不能为空！");
        }
        if (image.getSize() > 1024 * 1024 * 2) {
            throw new StatusFailException("上传的头像图片文件大小不能大于2M！");
        }
        // 获取文件后缀
        String suffix = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".") + 1);
        if (!"jpg,jpeg,gif,png,webp".toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请选择jpg,jpeg,gif,png,webp格式的头像图片！");
        }
        // 若不存在该目录，则创建目录
        FileUtil.mkdir(filePathProps.getUserAvatarFolder());
        // 通过UUID生成唯一文件名
        String filename = IdUtil.simpleUUID() + "." + suffix;
        try {
            // 将文件保存指定目录
            image.transferTo(FileUtil.file(filePathProps.getUserAvatarFolder() + File.separator + filename));
        } catch (Exception e) {
            log.error("头像文件上传异常-------------->", e);
            throw new StatusSystemErrorException("服务器异常：头像上传失败！");
        }

        // 获取当前登录用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        // 将当前用户所属的file表中avatar类型的实体的delete设置为1；
        fileEntityService.updateFileToDeleteByUidAndType(userRolesVO.getUid(), "avatar");

        // 更新user_info里面的avatar
        UpdateWrapper<UserInfo> userInfoUpdateWrapper = new UpdateWrapper<>();
        userInfoUpdateWrapper.set("avatar", filePathProps.getImgApi() + filename).eq("uuid", userRolesVO.getUid());
        userInfoEntityService.update(userInfoUpdateWrapper);

        // 插入file表记录
        com.simplefanc.voj.common.pojo.entity.common.File imgFile = new com.simplefanc.voj.common.pojo.entity.common.File();
        imgFile.setName(filename).setFolderPath(filePathProps.getUserAvatarFolder())
                .setFilePath(filePathProps.getUserAvatarFolder() + File.separator + filename).setSuffix(suffix)
                .setType(FileTypeEnum.AVATAR.getType()).setUid(userRolesVO.getUid());
        fileEntityService.saveOrUpdate(imgFile);

        // 更新session
        userRolesVO.setAvatar(filePathProps.getImgApi() + filename);
        UserSessionUtil.setUserInfo(userRolesVO);
        return MapUtil.builder().put("uid", userRolesVO.getUid()).put("username", userRolesVO.getUsername())
                .put("nickname", userRolesVO.getNickname()).put("avatar", filePathProps.getImgApi() + filename)
                .put("email", userRolesVO.getEmail()).put("number", userRolesVO.getNumber())
                .put("school", userRolesVO.getSchool()).put("course", userRolesVO.getCourse())
                .put("signature", userRolesVO.getSignature()).put("realname", userRolesVO.getRealname())
                .put("github", userRolesVO.getGithub()).put("blog", userRolesVO.getBlog())
                .put("cfUsername", userRolesVO.getCfUsername())
                .put("roleList", userRolesVO.getRoles().stream().map(Role::getRole)).map();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Object, Object> uploadCarouselImg(MultipartFile image) {

        if (image == null) {
            throw new StatusFailException("上传的图片文件不能为空！");
        }

        // 获取文件后缀
        String suffix = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".") + 1);
        if (!IMAGE_FORMAT.toUpperCase().contains(suffix.toUpperCase())) {
            throw new StatusFailException("请选择" + IMAGE_FORMAT + "格式的头像图片！");
        }
        // 若不存在该目录，则创建目录
        FileUtil.mkdir(filePathProps.getHomeCarouselFolder());
        // 通过UUID生成唯一文件名
        String filename = IdUtil.simpleUUID() + "." + suffix;
        try {
            // 将文件保存指定目录
            image.transferTo(FileUtil.file(filePathProps.getHomeCarouselFolder() + File.separator + filename));
        } catch (Exception e) {
            log.error("图片文件上传异常-------------->", e);
            throw new StatusSystemErrorException("服务器异常：图片上传失败！");
        }

        // 获取当前登录用户
        UserRolesVO userRolesVO = UserSessionUtil.getUserInfo();

        // 插入file表记录
        com.simplefanc.voj.common.pojo.entity.common.File imgFile = new com.simplefanc.voj.common.pojo.entity.common.File();
        imgFile.setName(filename).setFolderPath(filePathProps.getHomeCarouselFolder())
                .setFilePath(filePathProps.getHomeCarouselFolder() + File.separator + filename)
                .setSuffix(suffix)
                .setType(FileTypeEnum.CAROUSEL.getType())
                .setUid(userRolesVO.getUid());
        fileEntityService.saveOrUpdate(imgFile);

        return MapUtil.builder().put("id", imgFile.getId()).put("url", filePathProps.getImgApi() + filename).map();
    }

}