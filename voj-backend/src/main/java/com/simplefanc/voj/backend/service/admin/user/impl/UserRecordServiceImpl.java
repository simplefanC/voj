package com.simplefanc.voj.backend.service.admin.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.service.admin.user.UserRecordService;
import com.simplefanc.voj.backend.mapper.UserRecordMapper;
import com.simplefanc.voj.backend.pojo.vo.ACMRankVO;
import com.simplefanc.voj.backend.pojo.vo.OIRankVO;
import com.simplefanc.voj.backend.pojo.vo.UserHomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @Author: chenfan
 * @since 2021-10-23
 */
@Service
@RequiredArgsConstructor
public class UserRecordServiceImpl implements UserRecordService {

    private final UserRecordMapper userRecordMapper;

    @Override
    public List<ACMRankVO> getRecent7ACRank() {
        return userRecordMapper.getRecent7ACRank();
    }

    @Override
    public UserHomeVO getUserHomeInfo(String uid, String username) {
        return userRecordMapper.getUserHomeInfo(uid, username);
    }

    @Override
    public IPage<OIRankVO> getOIRankList(Page<OIRankVO> page, List<String> uidList) {
        return userRecordMapper.getOIRankList(page, uidList);
    }

    @Override
    public IPage<ACMRankVO> getACMRankList(Page<ACMRankVO> page, List<String> uidList) {
        return userRecordMapper.getACMRankList(page, uidList);
    }

}
