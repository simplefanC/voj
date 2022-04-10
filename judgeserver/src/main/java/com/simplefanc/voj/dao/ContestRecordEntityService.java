package com.simplefanc.voj.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.contest.ContestRecord;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author chenfan
 * @since 2020-10-23
 */
public interface ContestRecordEntityService extends IService<ContestRecord> {
    void UpdateContestRecord(String uid, Integer score, Integer status, Long submitId, Long cid, Integer useTime);
}
