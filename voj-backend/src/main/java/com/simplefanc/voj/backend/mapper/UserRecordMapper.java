package com.simplefanc.voj.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.pojo.vo.ACMRankVo;
import com.simplefanc.voj.backend.pojo.vo.OIRankVo;
import com.simplefanc.voj.backend.pojo.vo.UserHomeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Mapper
public interface UserRecordMapper {

    IPage<ACMRankVo> getACMRankList(Page<ACMRankVo> page, @Param("uidList") List<String> uidList);

    List<ACMRankVo> getRecent7ACRank();

    IPage<OIRankVo> getOIRankList(Page<OIRankVo> page, @Param("uidList") List<String> uidList);

    UserHomeVo getUserHomeInfo(@Param("uid") String uid, @Param("username") String username);

}
