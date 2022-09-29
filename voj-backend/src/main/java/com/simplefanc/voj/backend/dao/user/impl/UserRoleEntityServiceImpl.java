package com.simplefanc.voj.backend.dao.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simplefanc.voj.backend.common.constants.RoleEnum;
import com.simplefanc.voj.backend.dao.user.UserRoleEntityService;
import com.simplefanc.voj.backend.mapper.UserRoleMapper;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.shiro.AccountProfile;
import com.simplefanc.voj.common.pojo.entity.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.LogoutAware;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
public class UserRoleEntityServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleEntityService {

    private final static List<String> CHINESE_ROLE = Arrays.asList("超级管理员", "普通管理员", "普通用户(默认)", "普通用户(禁止提交)",
            "普通用户(禁止发讨论)", "普通用户(禁言)", "普通用户(禁止提交&禁止发讨论)", "用户(禁止提交&禁言)", "题目管理员");

    private final static List<String> ENGLISH_ROLE = Arrays.asList("Super Administrator", "General Administrator",
            "Normal User(Default)", "Normal User(No Submission)", "Normal User(No Discussion)",
            "Normal User(Forbidden Words)", "Normal User(No Submission & No Discussion)",
            "Normal User(No Submission & Forbidden Words)", "Problem Administrator");

    private final UserRoleMapper userRoleMapper;

    private final RedisSessionDAO redisSessionDAO;

    @Override
    public UserRolesVo getUserRoles(String uid, String username) {
        return userRoleMapper.getUserRoles(uid, username);
    }

    @Override
    public IPage<UserRolesVo> getUserList(int limit, int currentPage, String keyword, Long roleId, Integer status) {
        // 新建分页
        Page<UserRolesVo> page = new Page<>(currentPage, limit);
//        if (onlyAdmin) {
//            return userRoleMapper.getAdminUserList(page, limit, currentPage, keyword,
//                    Arrays.asList(RoleEnum.ROOT.getId(), RoleEnum.PROBLEM_ADMIN.getId(), RoleEnum.ADMIN.getId()));
//        } else {
            return userRoleMapper.getUserList(page, limit, currentPage, keyword, roleId, status);
//        }
    }

    /**
     * @param uid             当前需要操作的用户id
     * @param isRemoveSession 如果为true则会强行删除该用户session，必须重新登陆，false的话 在访问受限接口时会重新授权
     * @MethodName deleteCache
     * @Description
     * @Return
     * @Since 2021/6/12
     */
    @Override
    public void deleteCache(String uid, boolean isRemoveSession) {
        // 从缓存中获取Session
        Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        for (Session sessionInfo : sessions) {
            // 遍历Session,找到该用户名称对应的Session
            Object attribute = sessionInfo.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
            if (attribute == null) {
                continue;
            }
            AccountProfile accountProfile = (AccountProfile) ((SimplePrincipalCollection) attribute)
                    .getPrimaryPrincipal();
            if (accountProfile == null) {
                continue;
            }
            // 如果该session是指定的uid用户的
            if (Objects.equals(accountProfile.getUid(), uid)) {
                deleteSession(isRemoveSession, sessionInfo, attribute);
            }
        }

    }

    private void deleteSession(boolean isRemoveSession, Session session, Object attribute) {
        // 删除session 会强制退出！主要是在禁用用户或角色时，强制用户退出的
        if (isRemoveSession) {
            redisSessionDAO.delete(session);
        }

        // 删除Cache，在访问受限接口时会重新授权
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        Authenticator authc = securityManager.getAuthenticator();
        ((LogoutAware) authc).onLogout((SimplePrincipalCollection) attribute);
    }

    @Override
    public String getAuthChangeContent(int oldType, int newType) {
        String msg = "您好，您的权限产生了变更，由【" + CHINESE_ROLE.get(oldType - 1000) + "】变更为【" + CHINESE_ROLE.get(newType - 1000)
                + "】。部分权限可能与之前有所不同，请您注意！" + "\n\n" + "Hello, your permission has been changed from 【"
                + ENGLISH_ROLE.get(oldType - 1000) + "】 to 【" + ENGLISH_ROLE.get(newType - 1000)
                + "】. Some permissions may be different from before. Please note!";
        return msg;
    }

}
