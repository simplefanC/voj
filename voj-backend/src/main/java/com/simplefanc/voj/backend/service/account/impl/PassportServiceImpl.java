package com.simplefanc.voj.backend.service.account.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.backend.common.constants.EmailConstant;
import com.simplefanc.voj.backend.common.constants.RoleEnum;
import com.simplefanc.voj.backend.common.exception.StatusAccessDeniedException;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.utils.JwtUtil;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.user.SessionEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.dao.user.UserRoleEntityService;
import com.simplefanc.voj.backend.config.property.EmailRuleBO;
import com.simplefanc.voj.backend.pojo.dto.ApplyResetPasswordDTO;
import com.simplefanc.voj.backend.pojo.dto.LoginDTO;
import com.simplefanc.voj.backend.pojo.dto.RegisterDTO;
import com.simplefanc.voj.backend.pojo.dto.ResetPasswordDTO;
import com.simplefanc.voj.backend.config.ConfigVO;
import com.simplefanc.voj.backend.pojo.vo.RegisterCodeVO;
import com.simplefanc.voj.backend.pojo.vo.UserInfoVO;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.service.account.PassportService;
import com.simplefanc.voj.backend.service.email.EmailService;
import com.simplefanc.voj.backend.service.msg.NoticeService;
import com.simplefanc.voj.common.constants.RedisConstant;
import com.simplefanc.voj.common.pojo.entity.user.*;
import com.simplefanc.voj.common.utils.IpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:46
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class PassportServiceImpl implements PassportService {

    private final RedisUtil redisUtil;

    private final JwtUtil jwtUtil;

    private final ConfigVO configVO;

    private final EmailRuleBO emailRuleBo;

    private final UserInfoEntityService userInfoEntityService;

    private final UserRoleEntityService userRoleEntityService;

    private final SessionEntityService sessionEntityService;

    private final EmailService emailService;

    private final NoticeService noticeService;

    @Override
    public UserInfoVO login(LoginDTO loginDTO, HttpServletResponse response, HttpServletRequest request) {
        // 去掉账号密码首尾的空格
        loginDTO.setPassword(loginDTO.getPassword().trim());
        loginDTO.setUsername(loginDTO.getUsername().trim());

        String userIpAddr = IpUtil.getUserIpAddr(request);
        String key = RedisConstant.TRY_LOGIN_NUM + loginDTO.getUsername() + "_" + userIpAddr;
        Integer tryLoginCount = redisUtil.get(key, Integer.class);

        if (tryLoginCount != null && tryLoginCount >= 20) {
            throw new StatusFailException("对不起！登录失败次数过多！您的账号有风险，半个小时内暂时无法登录！");
        }

        UserRolesVO userRolesVO = userRoleEntityService.getUserRoles(null, loginDTO.getUsername());

        if (userRolesVO == null) {
            throw new StatusFailException("用户名或密码错误！请注意大小写！");
        }

        if (!userRolesVO.getPassword().equals(SecureUtil.md5(loginDTO.getPassword()))) {
            if (tryLoginCount == null) {
                // 三十分钟不尝试，该限制会自动清空消失
                redisUtil.set(key, 1, 60 * 30);
            } else {
                redisUtil.set(key, tryLoginCount + 1, 60 * 30);
            }
            throw new StatusFailException("用户名或密码错误！请注意大小写！");
        }

        if (userRolesVO.getStatus() != 0) {
            throw new StatusFailException("该账户暂未开放，请联系管理员进行处理！");
        }

        String jwt = jwtUtil.generateToken(userRolesVO.getUid());
        // 放到信息头部
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        // 会话记录
        sessionEntityService.save(new Session().setUid(userRolesVO.getUid()).setIp(IpUtil.getUserIpAddr(request))
                .setUserAgent(request.getHeader("User-Agent")));

        // 登录成功，清除锁定限制
        if (tryLoginCount != null) {
            redisUtil.del(key);
        }

        // 异步检查是否异地登录
//        sessionEntityService.checkRemoteLogin(userRolesVO.getUid());

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(userRolesVO, userInfoVO, "roles");
        userInfoVO.setRoleList(userRolesVO.getRoles().stream().map(Role::getRole).collect(Collectors.toList()));
        return userInfoVO;
    }

    @Override
    public RegisterCodeVO getRegisterCode(String email) {
        // 需要判断一下网站是否开启注册
        if (!configVO.getRegister()) {
            throw new StatusAccessDeniedException("对不起！本站暂未开启注册功能！");
        }
        if (!emailService.isOk()) {
            throw new StatusAccessDeniedException("对不起！本站邮箱系统未配置，暂不支持注册！");
        }

        email = email.trim();

        boolean isEmail = Validator.isEmail(email);
        if (!isEmail) {
            throw new StatusFailException("对不起！您的邮箱格式不正确！");
        }

        boolean isBlackEmail = emailRuleBo.getBlackList().stream().anyMatch(email::endsWith);
        if (isBlackEmail) {
            throw new StatusForbiddenException("对不起！您的邮箱无法注册本网站！");
        }

        String lockKey = EmailConstant.REGISTER_EMAIL_LOCK + email;
        if (redisUtil.hasKey(lockKey)) {
            throw new StatusFailException("对不起，您的操作频率过快，请在" + redisUtil.getExpire(lockKey) + "秒后再次发送注册邮件！");
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UserInfo userInfo = userInfoEntityService.getOne(queryWrapper, false);
        if (userInfo != null) {
            throw new StatusFailException("对不起！该邮箱已被注册，请更换新的邮箱！");
        }

        // 随机生成6位数字的组合
        String numbers = RandomUtil.randomNumbers(6);
        // 默认验证码有效5分钟
        redisUtil.set(EmailConstant.REGISTER_KEY_PREFIX + email, numbers, 5 * 60);
        emailService.sendCode(email, numbers);
        redisUtil.set(lockKey, 0, 60);

        RegisterCodeVO registerCodeVO = new RegisterCodeVO();
        registerCodeVO.setEmail(email);
        registerCodeVO.setExpire(5 * 60);

        return registerCodeVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        // 需要判断一下网站是否开启注册
        if (!configVO.getRegister()) {
            throw new StatusAccessDeniedException("对不起！本站暂未开启注册功能！");
        }

        String codeKey = EmailConstant.REGISTER_KEY_PREFIX + registerDTO.getEmail();
        if (!redisUtil.hasKey(codeKey)) {
            throw new StatusFailException("验证码不存在或已过期");
        }
        // 验证码判断
        if (!redisUtil.get(codeKey).equals(registerDTO.getCode())) {
            throw new StatusFailException("验证码不正确");
        }

        String uuid = IdUtil.simpleUUID();
        // 为新用户设置uuid
        registerDTO.setUuid(uuid);
        // 将密码MD5加密写入数据库
        registerDTO.setPassword(SecureUtil.md5(registerDTO.getPassword().trim()));
        registerDTO.setUsername(registerDTO.getUsername().trim());
        registerDTO.setEmail(registerDTO.getEmail().trim());

        // 往user_info表插入数据
        boolean addUser = userInfoEntityService.addUser(registerDTO);

        // 往user_role表插入数据
        boolean addUserRole = userRoleEntityService.save(new UserRole().setRoleId(RoleEnum.DEFAULT_USER.getId()).setUid(uuid));

        if (addUser && addUserRole) {
            redisUtil.del(registerDTO.getEmail());
            noticeService.syncNoticeToNewRegisterUser(uuid);
        } else {
            throw new StatusFailException("注册失败，请稍后重新尝试！");
        }
    }

    @Override
    public void applyResetPassword(ApplyResetPasswordDTO applyResetPasswordDTO) {
        String captcha = applyResetPasswordDTO.getCaptcha();
        String captchaKey = applyResetPasswordDTO.getCaptchaKey();
        String email = applyResetPasswordDTO.getEmail();

        if (!emailService.isOk()) {
            throw new StatusFailException("对不起！本站邮箱系统未配置，暂不支持重置密码！");
        }

        String lockKey = EmailConstant.RESET_EMAIL_LOCK + email;
        if (redisUtil.hasKey(lockKey)) {
            throw new StatusFailException("对不起，您的操作频率过快，请在" + redisUtil.getExpire(lockKey) + "秒后再次发送重置邮件！");
        }

        // 获取redis中的验证码
        String redisCode = redisUtil.get(captchaKey, String.class);
        if (!redisCode.equals(captcha.trim().toLowerCase())) {
            throw new StatusFailException("验证码不正确");
        }

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("email", email.trim());
        UserInfo userInfo = userInfoEntityService.getOne(userInfoQueryWrapper, false);
        if (userInfo == null) {
            throw new StatusFailException("对不起，该邮箱无该用户，请重新检查！");
        }
        // 随机生成20位数字与字母的组合
        String code = IdUtil.simpleUUID().substring(0, 21);
        // 默认链接有效10分钟
        redisUtil.set(EmailConstant.RESET_PASSWORD_KEY_PREFIX + userInfo.getUsername(), code, 10 * 60);
        // 发送邮件
        emailService.sendResetPassword(userInfo.getUsername(), code, email.trim());
        redisUtil.set(lockKey, 0, 90);
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String username = resetPasswordDTO.getUsername();
        String password = resetPasswordDTO.getPassword();
        String code = resetPasswordDTO.getCode();

        String codeKey = EmailConstant.RESET_PASSWORD_KEY_PREFIX + username;
        if (!redisUtil.hasKey(codeKey)) {
            throw new StatusFailException("重置密码链接不存在或已过期，请重新发送重置邮件");
        }
        // 验证码判断
        if (!redisUtil.get(codeKey).equals(code)) {
            throw new StatusFailException("重置密码的验证码不正确，请重新输入");
        }

        UpdateWrapper<UserInfo> userInfoUpdateWrapper = new UpdateWrapper<>();
        userInfoUpdateWrapper.eq("username", username).set("password", SecureUtil.md5(password));
        boolean isOk = userInfoEntityService.update(userInfoUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("重置密码失败");
        }
        redisUtil.del(codeKey);
    }

}