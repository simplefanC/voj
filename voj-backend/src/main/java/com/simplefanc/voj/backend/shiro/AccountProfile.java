package com.simplefanc.voj.backend.shiro;

import com.simplefanc.voj.common.pojo.entity.user.Role;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2020/7/19 22:59
 * @Description:
 */
@Data
public class AccountProfile implements Serializable {

    private String uid;

    private String username;

    private String password;

    private String nickname;

    private String school;

    private String course;

    private String number;

    private String gender;

    private String realname;

    private String cfUsername;

    private String email;

    private String avatar;

    private String signature;

    private int status;

    private Date gmtCreate;

    private Date gmtModified;

    private List<Role> roles;

    /**
     * shiro登录用户实体默认主键获取方法要为getId
     *
     * @return
     */
    public String getId() {
        return uid;
    }

}