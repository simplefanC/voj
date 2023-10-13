package com.simplefanc.voj.backend.dao.contest.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.mapper.ContestMapper;
import com.simplefanc.voj.backend.pojo.vo.ContestRegisterCountVO;
import com.simplefanc.voj.backend.pojo.vo.ContestVO;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
public class ContestEntityServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestEntityService {

    private final ContestMapper contestMapper;
    private final ContestValidator contestValidator;

    @Override
    public List<ContestVO> getWithinNext14DaysContests() {
        List<Contest> contestList = contestMapper.getWithinNext14DaysContests();

        final List<ContestVO> contestVOList = contestList.stream()
                .filter(contestValidator::checkVisible)
                .map(contest -> BeanUtil.copyProperties(contest, ContestVO.class))
                .collect(Collectors.toList());

        setRegisterCount(contestVOList);

        return contestVOList;
    }

    @Override
    public IPage<ContestVO> getContestList(Integer limit, Integer currentPage, Integer type, Integer status,
                                           String keyword) {
        // 新建分页
        IPage<ContestVO> page = new Page<>(currentPage, limit);

        List<Contest> contestList = contestMapper.getContestList(page, type, status, keyword);

        final List<ContestVO> contestVOList = contestList.stream()
                .filter(contestValidator::checkVisible)
                .map(contest -> BeanUtil.copyProperties(contest, ContestVO.class))
                .collect(Collectors.toList());

        setRegisterCount(contestVOList);

        return page.setRecords(contestVOList);
    }

    @Override
    public ContestVO getContestInfoById(long cid) {
        List<Long> cidList = Collections.singletonList(cid);
        Contest contest = contestMapper.selectById(cid);
        if (contestValidator.checkVisible(contest)) {
            ContestVO contestVO = BeanUtil.copyProperties(contest, ContestVO.class);
            List<ContestRegisterCountVO> contestRegisterCountVOList = contestMapper.getContestRegisterCount(cidList);
            if (!CollectionUtils.isEmpty(contestRegisterCountVOList)) {
                ContestRegisterCountVO contestRegisterCountVO = contestRegisterCountVOList.get(0);
                contestVO.setCount(contestRegisterCountVO.getCount());
            }
            return contestVO;
        }
        return null;
    }

    /**
     * 获取比赛注册人数
     * @param contestList
     */
    private void setRegisterCount(List<ContestVO> contestList) {
        List<Long> cidList = contestList.stream().map(ContestVO::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cidList)) {
            List<ContestRegisterCountVO> contestRegisterCountVOList = contestMapper.getContestRegisterCount(cidList);
            for (ContestRegisterCountVO contestRegisterCountVO : contestRegisterCountVOList) {
                for (ContestVO contestVO : contestList) {
                    if (contestRegisterCountVO.getCid().equals(contestVO.getId())) {
                        contestVO.setCount(contestRegisterCountVO.getCount());
                        break;
                    }
                }
            }
        }
    }
}
