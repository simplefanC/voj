package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.backend.common.constants.AccountConstant;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusSystemErrorException;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.problem.ProblemEntityService;
import com.simplefanc.voj.backend.dao.user.*;
import com.simplefanc.voj.backend.pojo.dto.ChangeEmailDto;
import com.simplefanc.voj.backend.pojo.dto.ChangePasswordDto;
import com.simplefanc.voj.backend.pojo.dto.CheckUsernameOrEmailDto;
import com.simplefanc.voj.backend.pojo.vo.*;
import com.simplefanc.voj.backend.service.oj.AccountService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.problem.Problem;
import com.simplefanc.voj.common.pojo.entity.user.Role;
import com.simplefanc.voj.common.pojo.entity.user.Session;
import com.simplefanc.voj.common.pojo.entity.user.UserAcproblem;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 16:53
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final RedisUtil redisUtil;

    private final UserInfoEntityService userInfoEntityService;

    private final UserRoleEntityService userRoleEntityService;

    private final UserRecordEntityService userRecordEntityService;

    private final UserAcproblemEntityService userAcproblemEntityService;

    private final ProblemEntityService problemEntityService;

    private final SessionEntityService sessionEntityService;

    /**
     * @MethodName checkUsernameOrEmail
     * @Params * @param null
     * @Description 检验用户名和邮箱是否存在
     * @Return
     * @Since 2021/11/5
     */
    @Override
    public CheckUsernameOrEmailVo checkUsernameOrEmail(CheckUsernameOrEmailDto checkUsernameOrEmailDto) {

        String email = checkUsernameOrEmailDto.getEmail();

        String username = checkUsernameOrEmailDto.getUsername();

        boolean rightEmail = false;

        boolean rightUsername = false;

        if (!StrUtil.isEmpty(email)) {
            email = email.trim();
            boolean isEmail = Validator.isEmail(email);
            if (!isEmail) {
                rightEmail = false;
            } else {
                QueryWrapper<UserInfo> wrapper = new QueryWrapper<UserInfo>().eq("email", email);
                UserInfo user = userInfoEntityService.getOne(wrapper, false);
                if (user != null) {
                    rightEmail = true;
                } else {
                    rightEmail = false;
                }
            }
        }

        if (!StrUtil.isEmpty(username)) {
            username = username.trim();
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<UserInfo>().eq("username", username);
            UserInfo user = userInfoEntityService.getOne(wrapper, false);
            if (user != null) {
                rightUsername = true;
            } else {
                rightUsername = false;
            }
        }

        CheckUsernameOrEmailVo checkUsernameOrEmailVo = new CheckUsernameOrEmailVo();
        checkUsernameOrEmailVo.setEmail(rightEmail);
        checkUsernameOrEmailVo.setUsername(rightUsername);
        return checkUsernameOrEmailVo;
    }

    /**
     * @param uid
     * @MethodName getUserHomeInfo
     * @Description 前端userHome用户个人主页的数据请求，主要是返回解决题目数，AC的题目列表，提交总数，AC总数，Rating分，
     * @Return CommonResult
     * @Since 2021/01/07
     */
    @Override
    public UserHomeVo getUserHomeInfo(String uid, String username) {

        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        // 如果没有uid和username，默认查询当前登录用户的
        if (StrUtil.isEmpty(uid) && StrUtil.isEmpty(username)) {
            if (userRolesVo != null) {
                uid = userRolesVo.getUid();
            } else {
                throw new StatusFailException("错误：uid和username不能都为空！");
            }
        }

        UserHomeVo userHomeInfo = userRecordEntityService.getUserHomeInfo(uid, username);
        if (userHomeInfo == null) {
            throw new StatusFailException("用户不存在");
        }
        QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", userHomeInfo.getUid()).select("distinct pid");
        List<Long> pidList = new LinkedList<>();
        List<UserAcproblem> acProblemList = userAcproblemEntityService.list(queryWrapper);
        acProblemList.forEach(acProblem -> {
            pidList.add(acProblem.getPid());
        });

        List<String> disPlayIdList = new LinkedList<>();

        if (pidList.size() > 0) {
            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.in("id", pidList);
            List<Problem> problems = problemEntityService.list(problemQueryWrapper);
            problems.forEach(problem -> {
                disPlayIdList.add(problem.getProblemId());
            });
        }

        userHomeInfo.setSolvedList(disPlayIdList);
        QueryWrapper<Session> sessionQueryWrapper = new QueryWrapper<>();
        sessionQueryWrapper.eq("uid", userHomeInfo.getUid()).orderByDesc("gmt_create").last("limit 1");

        Session recentSession = sessionEntityService.getOne(sessionQueryWrapper, false);
        if (recentSession != null) {
            userHomeInfo.setRecentLoginTime(recentSession.getGmtCreate());
        }
        return userHomeInfo;
    }

    /**
     * @MethodName changePassword
     * @Description 修改密码的操作，连续半小时内修改密码错误5次，则需要半个小时后才可以再次尝试修改密码
     * @Return
     * @Since 2021/1/8
     */
    @Override
    public ChangeAccountVo changePassword(ChangePasswordDto changePasswordDto) {
        String oldPassword = changePasswordDto.getOldPassword();
        String newPassword = changePasswordDto.getNewPassword();

        // 数据可用性判断
        if (StrUtil.isEmpty(oldPassword) || StrUtil.isEmpty(newPassword)) {
            throw new StatusFailException("错误：原始密码或新密码不能为空！");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new StatusFailException("新密码长度应该为6~20位！");
        }
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        // 如果已经被锁定半小时不能修改
        String lockKey = AccountConstant.CODE_CHANGE_PASSWORD_LOCK + userRolesVo.getUid();
        // 统计失败的key
        String countKey = AccountConstant.CODE_CHANGE_PASSWORD_FAIL + userRolesVo.getUid();

        ChangeAccountVo resp = new ChangeAccountVo();
        if (redisUtil.hasKey(lockKey)) {
            long expire = redisUtil.getExpire(lockKey);
            Date now = new Date();
            long minute = expire / 60;
            long second = expire % 60;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resp.setCode(403);
            Date afterDate = new Date(now.getTime() + expire * 1000);
            String msg = "由于您多次修改密码失败，修改密码功能已锁定，请在" + minute + "分" + second + "秒后(" + formatter.format(afterDate)
                    + ")再进行尝试！";
            resp.setMsg(msg);
            return resp;
        }
        // 与当前登录用户的密码进行比较判断
        // 如果相同，则进行修改密码操作
        if (userRolesVo.getPassword().equals(SecureUtil.md5(oldPassword))) {
            UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
            // 数据库用户密码全部用md5加密
            updateWrapper.set("password", SecureUtil.md5(newPassword)).eq("uuid", userRolesVo.getUid());
            boolean isOk = userInfoEntityService.update(updateWrapper);
            if (isOk) {
                resp.setCode(200);
                resp.setMsg("修改密码成功！您将于5秒钟后退出进行重新登录操作！");
                // 清空记录
                redisUtil.del(countKey);
                // 更新session
                userRolesVo.setPassword(SecureUtil.md5(newPassword));
                UserSessionUtil.setUserInfo(userRolesVo);
                return resp;
            } else {
                throw new StatusSystemErrorException("系统错误：修改密码失败！");
            }
        } else {
            // 如果不同，则进行记录，当失败次数达到5次，半个小时后才可重试
            Integer count = redisUtil.get(countKey, Integer.class);
            if (count == null) {
                // 三十分钟不尝试，该限制会自动清空消失
                redisUtil.set(countKey, 1, 60 * 30);
                count = 0;
            } else if (count < 5) {
                redisUtil.incr(countKey, 1);
            }
            count++;
            if (count == 5) {
                // 清空统计
                redisUtil.del(countKey);
                // 设置锁定更改
                redisUtil.set(lockKey, "lock", 60 * 30);
            }
            resp.setCode(400);
            resp.setMsg("原始密码错误！您已累计修改密码失败" + count + "次...");
            return resp;
        }
    }

    /**
     * @MethodName changeEmail
     * @Description 修改邮箱的操作，连续半小时内密码错误5次，则需要半个小时后才可以再次尝试修改
     * @Return
     * @Since 2021/1/9
     */
    @Override
    public ChangeAccountVo changeEmail(ChangeEmailDto changeEmailDto) {

        String password = changeEmailDto.getPassword();
        String newEmail = changeEmailDto.getNewEmail();
        // 数据可用性判断
        if (StrUtil.isEmpty(password) || StrUtil.isEmpty(newEmail)) {
            throw new StatusFailException("错误：密码或新邮箱不能为空！");
        }
        if (!Validator.isEmail(newEmail)) {
            throw new StatusFailException("邮箱格式错误！");
        }
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        // 如果已经被锁定半小时不能修改
        String lockKey = AccountConstant.CODE_CHANGE_EMAIL_LOCK + userRolesVo.getUid();
        // 统计失败的key
        String countKey = AccountConstant.CODE_CHANGE_EMAIL_FAIL + userRolesVo.getUid();

        ChangeAccountVo resp = new ChangeAccountVo();
        if (redisUtil.hasKey(lockKey)) {
            long expire = redisUtil.getExpire(lockKey);
            Date now = new Date();
            long minute = expire / 60;
            long second = expire % 60;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resp.setCode(403);
            Date afterDate = new Date(now.getTime() + expire * 1000);
            String msg = "由于您多次修改邮箱失败，修改邮箱功能已锁定，请在" + minute + "分" + second + "秒后(" + formatter.format(afterDate)
                    + ")再进行尝试！";
            resp.setMsg(msg);
            return resp;
        }
        // 与当前登录用户的密码进行比较判断
        // 如果相同，则进行修改操作
        if (userRolesVo.getPassword().equals(SecureUtil.md5(password))) {
            UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("email", newEmail).eq("uuid", userRolesVo.getUid());

            boolean isOk = userInfoEntityService.update(updateWrapper);
            if (isOk) {
                UserInfoVo userInfoVo = new UserInfoVo();
                BeanUtil.copyProperties(userRolesVo, userInfoVo, "roles");
                userInfoVo.setRoleList(userRolesVo.getRoles().stream().map(Role::getRole).collect(Collectors.toList()));

                resp.setCode(200);
                resp.setMsg("修改邮箱成功！");
                resp.setUserInfo(userInfoVo);
                // 清空记录
                redisUtil.del(countKey);
                // 更新session
                userRolesVo.setEmail(newEmail);
                UserSessionUtil.setUserInfo(userRolesVo);
                return resp;
            } else {
                throw new StatusSystemErrorException("系统错误：修改邮箱失败！");
            }
        } else {
            // 如果不同，则进行记录，当失败次数达到5次，半个小时后才可重试
            Integer count = redisUtil.get(countKey, Integer.class);
            if (count == null) {
                // 三十分钟不尝试，该限制会自动清空消失
                redisUtil.set(countKey, 1, 60 * 30);
                count = 0;
            } else if (count < 5) {
                redisUtil.incr(countKey, 1);
            }
            count++;
            if (count == 5) {
                // 清空统计
                redisUtil.del(countKey);
                // 设置锁定更改
                redisUtil.set(lockKey, "lock", 60 * 30);
            }

            resp.setCode(400);
            resp.setMsg("密码错误！您已累计修改邮箱失败" + count + "次...");
            return resp;
        }
    }

    @Override
    public UserInfoVo changeUserInfo(UserInfoVo userInfoVo) {

        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        String realname = userInfoVo.getRealname();
        String nickname = userInfoVo.getNickname();
        if (!StrUtil.isEmpty(realname) && realname.length() > 50) {
            throw new StatusFailException("真实姓名的长度不能超过50位");
        }
        if (!StrUtil.isEmpty(nickname) && nickname.length() > 20) {
            throw new StatusFailException("昵称的长度不能超过20位");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUuid(userRolesVo.getUid()).setCfUsername(userInfoVo.getCfUsername()).setRealname(realname)
                .setNickname(nickname).setSignature(userInfoVo.getSignature()).setBlog(userInfoVo.getBlog())
                .setGender(userInfoVo.getGender()).setEmail(userRolesVo.getEmail()).setGithub(userInfoVo.getGithub())
                .setSchool(userInfoVo.getSchool()).setNumber(userInfoVo.getNumber());

        boolean isOk = userInfoEntityService.updateById(userInfo);

        if (isOk) {
            // 更新session
            UserRolesVo userRoles = userRoleEntityService.getUserRoles(userRolesVo.getUid(), null);
            UserSessionUtil.setUserInfo(userRoles);

            UserInfoVo userInfoRes = new UserInfoVo();
            BeanUtil.copyProperties(userRoles, userInfoRes, "roles");
            userInfoVo.setRoleList(userRoles.getRoles().stream().map(Role::getRole).collect(Collectors.toList()));

            return userInfoRes;
        } else {
            throw new StatusFailException("更新个人信息失败！");
        }

    }

}