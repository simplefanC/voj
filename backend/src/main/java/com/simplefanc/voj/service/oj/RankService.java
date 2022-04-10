package com.simplefanc.voj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 20:47
 * @Description:
 */

public interface RankService {


    /**
     * @MethodName get-rank-list
     * @Params * @param null
     * @Description 获取排行榜数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    IPage getRankList(Integer limit, Integer currentPage, String searchUser, Integer type);


}