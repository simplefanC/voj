package com.simplefanc.voj.judger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simplefanc.voj.common.pojo.entity.user.UserRecord;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author chenfan
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface UserRecordMapper extends BaseMapper<UserRecord> {

}
