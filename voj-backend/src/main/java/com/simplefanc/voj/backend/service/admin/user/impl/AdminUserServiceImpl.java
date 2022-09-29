package com.simplefanc.voj.backend.service.admin.user.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.constants.AccountConstant;
import com.simplefanc.voj.backend.common.constants.Constant;
import com.simplefanc.voj.backend.common.constants.RoleEnum;
import com.simplefanc.voj.backend.common.constants.UserStatusEnum;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.service.admin.user.UserRecordService;
import com.simplefanc.voj.backend.dao.user.UserRoleEntityService;
import com.simplefanc.voj.backend.pojo.dto.AdminEditUserDto;
import com.simplefanc.voj.backend.pojo.vo.ExcelUserVo;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.admin.user.AdminUserService;
import com.simplefanc.voj.backend.service.msg.AdminNoticeService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import com.simplefanc.voj.common.pojo.entity.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 21:05
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRoleEntityService userRoleEntityService;

    private final UserInfoEntityService userInfoEntityService;

    private final AdminNoticeService adminNoticeService;

    private final RedisUtil redisUtil;

    @Override
    public IPage<UserRolesVo> getUserList(Integer limit, Integer currentPage, String keyword, Long roleId, Integer status) {
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;
        if (keyword != null) {
            keyword = keyword.trim();
        }
        return userRoleEntityService.getUserList(limit, currentPage, keyword, roleId, status);
    }

    @Override
    public void editUser(AdminEditUserDto adminEditUserDto) {
        String uid = adminEditUserDto.getUid();

        UpdateWrapper<UserInfo> userInfoUpdateWrapper = new UpdateWrapper<>();

        userInfoUpdateWrapper.eq("uuid", uid)
                .set("username", adminEditUserDto.getUsername())
                .set("realname", adminEditUserDto.getRealname())
                .set("school", adminEditUserDto.getSchool())
                .set("number", adminEditUserDto.getNumber())
                .set("email", adminEditUserDto.getEmail())
                .set(adminEditUserDto.getSetNewPwd(), "password", SecureUtil.md5(adminEditUserDto.getPassword()))
                .set("status", adminEditUserDto.getStatus());
        boolean addUserInfo = userInfoEntityService.update(userInfoUpdateWrapper);

        QueryWrapper<UserRole> userRoleQueryWrapper = new QueryWrapper<>();
        userRoleQueryWrapper.eq("uid", uid);
        UserRole userRole = userRoleEntityService.getOne(userRoleQueryWrapper, false);
        boolean addUserRole = false;
        int type = adminEditUserDto.getType();
        int oldType = userRole.getRoleId().intValue();
        if (userRole.getRoleId().intValue() != type) {
            userRole.setRoleId((long) type);
            addUserRole = userRoleEntityService.updateById(userRole);
            if (type == RoleEnum.ROOT.getId() || oldType == RoleEnum.ROOT.getId()) {
                // 新增或者去除超级管理员需要删除缓存
                String cacheKey = AccountConstant.SUPER_ADMIN_UID_LIST_CACHE;
                redisUtil.del(cacheKey);
            }
        }
        if (addUserInfo) {
            // 需要重新登录
            userRoleEntityService.deleteCache(uid, true);
        } else if (addUserRole) {
            // 需要重新授权
            userRoleEntityService.deleteCache(uid, false);
        }

        if (addUserRole) {
            // 获取当前登录的用户
            UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
            String title = "权限变更通知(Authority Change Notice)";
            String content = userRoleEntityService.getAuthChangeContent(oldType, type);
            adminNoticeService.addSingleNoticeToUser(userRolesVo.getUid(), uid, title, content, "Sys");
        }

    }

    @Override
    public void deleteUser(List<String> deleteUserIdList) {
        boolean isOk = userInfoEntityService.removeByIds(deleteUserIdList);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }

    @Override
    public void forbidUser(List<String> userIdList) {
        final boolean isOk = userInfoEntityService.lambdaUpdate()
                .set(UserInfo::getStatus, UserStatusEnum.FORBID.getStatus())
                .in(UserInfo::getUuid, userIdList)
                .update();
        if (!isOk) {
            throw new StatusFailException("封禁失败！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBatchUser(List<List<String>> users) {
        List<UserInfo> userInfoList = new LinkedList<>();
        List<UserRole> userRoleList = new LinkedList<>();
        if (users != null) {
            for (List<String> user : users) {
                String uuid = IdUtil.simpleUUID();
                UserInfo userInfo = new UserInfo().setUuid(uuid).setUsername(user.get(0))
                        .setPassword(SecureUtil.md5(user.get(1)))
                        .setEmail(StrUtil.isEmpty(user.get(2)) ? null : user.get(2));

                if (user.size() >= 4) {
                    String realname = user.get(3);
                    if (!StrUtil.isEmpty(realname)) {
                        userInfo.setRealname(user.get(3));
                    }
                }

                if (user.size() >= 5) {
                    String gender = user.get(4);
                    if ("male".equals(gender.toLowerCase()) || "0".equals(gender)) {
                        userInfo.setGender("male");
                    } else if ("female".equals(gender.toLowerCase()) || "1".equals(gender)) {
                        userInfo.setGender("female");
                    }
                }

                if (user.size() >= 6) {
                    String nickname = user.get(5);
                    if (!StrUtil.isEmpty(nickname)) {
                        userInfo.setNickname(nickname);
                    }
                }

                if (user.size() >= 7) {
                    String school = user.get(6);
                    if (!StrUtil.isEmpty(school)) {
                        userInfo.setSchool(school);
                    }
                }

                userInfoList.add(userInfo);
                userRoleList.add(new UserRole()
                        .setRoleId(RoleEnum.DEFAULT_USER.getId())
                        .setUid(uuid));
            }
            boolean result1 = userInfoEntityService.saveBatch(userInfoList);
            boolean result2 = userRoleEntityService.saveBatch(userRoleList);
            if (result1 && result2) {
                // 异步同步系统通知
                List<String> uidList = userInfoList.stream().map(UserInfo::getUuid).collect(Collectors.toList());
                adminNoticeService.syncNoticeToNewRegisterBatchUser(uidList);
            } else {
                throw new StatusFailException("删除失败");
            }
        } else {
            throw new StatusFailException("插入的用户数据不能为空！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Object, Object> generateUser(Map<String, Object> params) {
        // TODO 参数
        String prefix = (String) params.getOrDefault("prefix", "");
        String suffix = (String) params.getOrDefault("suffix", "");
        int numberFrom = (int) params.getOrDefault("number_from", 1);
        int numberTo = (int) params.getOrDefault("number_to", 10);
        int passwordLength = (int) params.getOrDefault("password_length", 6);

        List<UserInfo> userInfoList = new LinkedList<>();
        List<UserRole> userRoleList = new LinkedList<>();
        List<ExcelUserVo> userVoList = new LinkedList<>();
        // 存储账号密码放入redis中，等待导出excel
        final int numLen = String.valueOf(numberTo).length();
        for (int num = numberFrom; num <= numberTo; num++) {
            String uuid = IdUtil.simpleUUID();
            String password = RandomUtil.randomString(passwordLength).toUpperCase();
            String username = prefix + String.format("%0" + numLen + "d", num) + suffix;
            userInfoList.add(new UserInfo().setUuid(uuid).setUsername(username).setPassword(SecureUtil.md5(password)));
            userVoList.add(new ExcelUserVo().setUsername(username).setPassword(password));
            userRoleList.add(new UserRole()
                    .setRoleId(RoleEnum.DEFAULT_USER.getId())
                    .setUid(uuid));
        }
        boolean result1 = userInfoEntityService.saveBatch(userInfoList);
        boolean result2 = userRoleEntityService.saveBatch(userRoleList);
        if (result1 && result2) {
            String key = IdUtil.simpleUUID();
            // 存储半小时
            redisUtil.hset(Constant.GENERATE_USER_INFO_LIST, key, userVoList, 1800);
            // 异步同步系统通知
            List<String> uidList = userInfoList.stream().map(UserInfo::getUuid).collect(Collectors.toList());
            adminNoticeService.syncNoticeToNewRegisterBatchUser(uidList);
            return MapUtil.builder().put("key", key).map();
        } else {
            throw new StatusFailException("生成指定用户失败！");
        }
    }

}