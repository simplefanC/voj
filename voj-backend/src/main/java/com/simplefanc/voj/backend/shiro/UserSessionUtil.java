package com.simplefanc.voj.backend.shiro;

import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;

/**
 * @author chenfan
 * @date 2022/4/13 11:29
 **/
public class UserSessionUtil {

    public static Boolean isProblemAdmin() {
        return SecurityUtils.getSubject().hasRole("problem_admin");
    }

    public static Boolean isRoot() {
        return SecurityUtils.getSubject().hasRole("root");
    }

    public static Boolean isAdmin() {
        return SecurityUtils.getSubject().hasRole("admin");
    }

    public static UserRolesVO getUserInfo() {
        return (UserRolesVO) getSessionAttribute("userInfo");
    }

    public static void logout() {
        SecurityUtils.getSubject().logout();
    }

    public static void setUserInfo(UserRolesVO userInfo) {
        setSessionAttribute("userInfo", userInfo);
    }

    private static Session getSession() {
        return SecurityUtils.getSubject().getSession();
    }

    private static void setSessionAttribute(Object key, Object value) {
        getSession().setAttribute(key, value);
    }

    private static Object getSessionAttribute(Object key) {
        return getSession().getAttribute(key);
    }

}
