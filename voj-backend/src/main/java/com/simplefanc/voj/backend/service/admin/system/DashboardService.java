package com.simplefanc.voj.backend.service.admin.system;

import com.simplefanc.voj.common.pojo.entity.user.Session;

import java.util.Map;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:44
 * @Description:
 */
public interface DashboardService {

    Session getRecentSession();

    Map<Object, Object> getDashboardInfo();
}