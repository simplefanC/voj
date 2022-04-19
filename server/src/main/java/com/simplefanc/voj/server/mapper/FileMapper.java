package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.common.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface FileMapper extends BaseMapper<File> {

    @Update("UPDATE `file` SET `delete` = 1 WHERE `uid` = #{uid} AND `type` = #{type}")
    int updateFileToDeleteByUidAndType(@Param("uid") String uid, @Param("type") String type);

    @Select("select * from file where (type = 'avatar' AND `delete` = true)")
    List<File> queryDeleteAvatarList();


    @Select("select * from file where (type = 'carousel')")
    List<File> queryCarouselFileList();


}
