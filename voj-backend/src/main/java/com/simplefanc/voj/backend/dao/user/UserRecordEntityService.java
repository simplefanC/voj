package com.simplefanc.voj.backend.dao.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.common.pojo.entity.user.UserRecord;
import com.simplefanc.voj.backend.pojo.vo.ACMRankVo;
import com.simplefanc.voj.backend.pojo.vo.OIRankVo;
import com.simplefanc.voj.backend.pojo.vo.UserHomeVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @Author: chenfan
 * @since 2020-10-23
 */
public interface UserRecordEntityService extends IService<UserRecord> {

    List<ACMRankVo> getRecent7ACRank();

    UserHomeVo getUserHomeInfo(String uid, String username);

    IPage<OIRankVo> getOIRankList(Page<OIRankVo> page, List<String> uidList);

    IPage<ACMRankVo> getACMRankList(Page<ACMRankVo> page, List<String> uidList);

}
