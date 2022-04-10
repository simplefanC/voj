package com.simplefanc.voj.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.simplefanc.voj.dao.contest.ContestPrintEntityService;
import com.simplefanc.voj.mapper.ContestPrintMapper;
import com.simplefanc.voj.pojo.entity.contest.ContestPrint;

/**
 * @Author: chenfan
 * @Date: 2021/9/19 21:05
 * @Description:
 */
@Service
public class ContestPrintEntityServiceImpl extends ServiceImpl<ContestPrintMapper, ContestPrint> implements ContestPrintEntityService {
}