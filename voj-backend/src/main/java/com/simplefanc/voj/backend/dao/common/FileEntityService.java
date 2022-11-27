package com.simplefanc.voj.backend.dao.common;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.common.File;

import java.util.List;

public interface FileEntityService extends IService<File> {

    int updateFileToDeleteByUidAndType(String uid, String type);

    List<File> queryDeleteAvatarList();

    List<File> queryCarouselFileList();

}
