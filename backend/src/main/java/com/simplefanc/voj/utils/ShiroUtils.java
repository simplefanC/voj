package com.simplefanc.voj.utils;

import org.apache.shiro.SecurityUtils;
import com.simplefanc.voj.shiro.AccountProfile;


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