package com.simplefanc.voj.dao.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.user.Session;

public interface SessionEntityService extends IService<Session> {

    void checkRemoteLogin(String uid);

}
