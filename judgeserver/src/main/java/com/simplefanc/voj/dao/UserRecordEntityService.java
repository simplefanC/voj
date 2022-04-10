package com.simplefanc.voj.dao;


import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.user.UserRecord;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author chenfan
 * @since 2020-10-23
 */
public interface UserRecordEntityService extends IService<UserRecord> {
    void updateRecord(String uid, Long submitId, Long pid, Integer score);
}
