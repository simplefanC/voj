package com.simplefanc.voj.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.common.pojo.entity.user.UserRecord;
import com.simplefanc.voj.server.pojo.vo.ACMRankVo;
import com.simplefanc.voj.server.pojo.vo.OIRankVo;
import com.simplefanc.voj.server.pojo.vo.UserHomeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface UserRecordMapper extends BaseMapper<UserRecord> {
    IPage<ACMRankVo> getACMRankList(Page<ACMRankVo> page, @Param("uidList") List<String> uidList);

    List<ACMRankVo> getRecent7ACRank();

    IPage<OIRankVo> getOIRankList(Page<OIRankVo> page, @Param("uidList") List<String> uidList);

    UserHomeVo getUserHomeInfo(@Param("uid") String uid, @Param("username") String username);

}
