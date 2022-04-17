package com.simplefanc.voj.utils;

import com.simplefanc.voj.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;


/**
 * @Author: chenfan
 * @Date: 2020/7/20 14:13
 * @Description:
 */
public class ShiroUtils {

    private ShiroUtils() {
    }

    public static AccountProfile getProfile() {
        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

}