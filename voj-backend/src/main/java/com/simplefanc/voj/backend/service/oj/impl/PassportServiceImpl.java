package com.simplefanc.voj.backend.service.oj.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.simplefanc.voj.common.pojo.entity.user.*;
import com.simplefanc.voj.common.utils.IpUtil;
import com.simplefanc.voj.backend.common.constants.AccountConstant;
import com.simplefanc.voj.backend.common.constants.EmailConstant;
import com.simplefanc.voj.backend.common.exception.StatusAccessDeniedException;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.utils.JwtUtil;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.user.SessionEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.dao.user.UserRecordEntityService;
import com.simplefanc.voj.backend.dao.user.UserRoleEntityService;
import com.simplefanc.voj.backend.pojo.bo.EmailRuleBo;
import com.simplefanc.voj.backend.pojo.dto.ApplyResetPasswordDto;
import com.simplefanc.voj.backend.pojo.dto.LoginDto;
import com.simplefanc.voj.backend.pojo.dto.RegisterDto;
import com.simplefanc.voj.backend.pojo.dto.ResetPasswordDto;
import com.simplefanc.voj.backend.pojo.vo.ConfigVo;
import com.simplefanc.voj.backend.pojo.vo.RegisterCodeVo;
import com.simplefanc.voj.backend.pojo.vo.UserInfoVo;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.service.email.EmailService;
import com.simplefanc.voj.backend.service.msg.NoticeService;
import com.simplefanc.voj.backend.service.oj.PassportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:46
 * @Description:
 */
@Service
public class PassportServiceImpl implements PassportService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private ConfigVo configVo;

    @Resource
    private EmailRuleBo emailRuleBo;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private UserRoleEntityService userRoleEntityService;

    @Resource
    private UserRecordEntityService userRecordEntityService;

    @Resource
    private SessionEntityService sessionEntityService;


    @Resource
    private EmailService emailService;

    @Resource
    private NoticeService noticeService;

    @Override
    public UserInfoVo login(LoginDto loginDto, HttpServletResponse response, HttpServletRequest request) {
        // 去掉账号密码首尾的空格
        loginDto.setPassword(loginDto.getPassword().trim());
        loginDto.setUsername(loginDto.getUsername().trim());
        if (loginDto.getPassword().length() < 6 || loginDto.getPassword().length() > 20) {
            throw new StatusFailException("密码长度应该为6~20位！");
        }
        if (loginDto.getUsername().length() > 20) {
            throw new StatusFailException("用户名长度不能超过20位!");
        }

        String userIpAddr = IpUtil.getUserIpAddr(request);
        String key = AccountConstant.TRY_LOGIN_NUM + loginDto.getUsername() + "_" + userIpAddr;
        Integer tryLoginCount = (Integer) redisUtil.get(key);

        if (tryLoginCount != null && tryLoginCount >= 20) {
            throw new StatusFailException("对不起！登录失败次数过多！您的账号有风险，半个小时内暂时无法登录！");
        }

        UserRolesVo userRolesVo = userRoleEntityService.getUserRoles(null, loginDto.getUsername());

        if (userRolesVo == null) {
            throw new StatusFailException("用户名或密码错误！请注意大小写！");
        }

        if (!userRolesVo.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            if (tryLoginCount == null) {
                // 三十分钟不尝试，该限制会自动清空消失
                redisUtil.set(key, 1, 60 * 30);
            } else {
                redisUtil.set(key, tryLoginCount + 1, 60 * 30);
            }
            throw new StatusFailException("用户名或密码错误！请注意大小写！");
        }

        if (userRolesVo.getStatus() != 0) {
            throw new StatusFailException("该账户已被封禁，请联系管理员进行处理！");
        }

        String jwt = jwtUtil.generateToken(userRolesVo.getUid());
        //放到信息头部
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        // 会话记录
        sessionEntityService.save(new Session()
                .setUid(userRolesVo.getUid())
                .setIp(IpUtil.getUserIpAddr(request))
                .setUserAgent(request.getHeader("User-Agent")));

        // 登录成功，清除锁定限制
        if (tryLoginCount != null) {
            redisUtil.del(key);
        }

        // 异步检查是否异地登录
        sessionEntityService.checkRemoteLogin(userRolesVo.getUid());

        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtil.copyProperties(userRolesVo, userInfoVo, "roles");
        userInfoVo.setRoleList(userRolesVo.getRoles().stream().map(Role::getRole).collect(Collectors.toList()));
        return userInfoVo;
    }


    @Override
    public RegisterCodeVo getRegisterCode(String email) {
        // 需要判断一下网站是否开启注册
        if (!configVo.getRegister()) {
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

        RegisterCodeVo registerCodeVo = new RegisterCodeVo();
        registerCodeVo.setEmail(email);
        registerCodeVo.setExpire(5 * 60);

        return registerCodeVo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDto registerDto) {
        // 需要判断一下网站是否开启注册
        if (!configVo.getRegister()) {
            throw new StatusAccessDeniedException("对不起！本站暂未开启注册功能！");
        }

        String codeKey = EmailConstant.REGISTER_KEY_PREFIX + registerDto.getEmail();
        if (!redisUtil.hasKey(codeKey)) {
            throw new StatusFailException("验证码不存在或已过期");
        }
        // 验证码判断
        if (!redisUtil.get(codeKey).equals(registerDto.getCode())) {
            throw new StatusFailException("验证码不正确");
        }

        if (registerDto.getPassword().length() < 6 || registerDto.getPassword().length() > 20) {
            throw new StatusFailException("密码长度应该为6~20位！");
        }

        if (registerDto.getUsername().length() > 20) {
            throw new StatusFailException("用户名长度不能超过20位!");
        }

        String uuid = IdUtil.simpleUUID();
        // 为新用户设置uuid
        registerDto.setUuid(uuid);
        // 将密码MD5加密写入数据库
        registerDto.setPassword(SecureUtil.md5(registerDto.getPassword().trim()));
        registerDto.setUsername(registerDto.getUsername().trim());
        registerDto.setEmail(registerDto.getEmail().trim());

        // 往user_info表插入数据
        boolean addUser = userInfoEntityService.addUser(registerDto);

        // 往user_role表插入数据
        boolean addUserRole = userRoleEntityService.save(new UserRole().setRoleId(1002L).setUid(uuid));

        // 往user_record表插入数据
        boolean addUserRecord = userRecordEntityService.save(new UserRecord().setUid(uuid));

        if (addUser && addUserRole && addUserRecord) {
            redisUtil.del(registerDto.getEmail());
            noticeService.syncNoticeToNewRegisterUser(uuid);
        } else {
            throw new StatusFailException("注册失败，请稍后重新尝试！");
        }
    }


    @Override
    public void applyResetPassword(ApplyResetPasswordDto applyResetPasswordDto) {

        String captcha = applyResetPasswordDto.getCaptcha();
        String captchaKey = applyResetPasswordDto.getCaptchaKey();
        String email = applyResetPasswordDto.getEmail();

        if (StringUtils.isEmpty(captcha) || StringUtils.isEmpty(email) || StringUtils.isEmpty(captchaKey)) {
            throw new StatusFailException("邮箱或验证码不能为空");
        }

        if (!emailService.isOk()) {
            throw new StatusFailException("对不起！本站邮箱系统未配置，暂不支持重置密码！");
        }

        String lockKey = EmailConstant.RESET_EMAIL_LOCK + email;
        if (redisUtil.hasKey(lockKey)) {
            throw new StatusFailException("对不起，您的操作频率过快，请在" + redisUtil.getExpire(lockKey) + "秒后再次发送重置邮件！");
        }

        // 获取redis中的验证码
        String redisCode = (String) redisUtil.get(captchaKey);
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
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        String username = resetPasswordDto.getUsername();
        String password = resetPasswordDto.getPassword();
        String code = resetPasswordDto.getCode();

        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(username) || StringUtils.isEmpty(code)) {
            throw new StatusFailException("用户名、新密码或验证码不能为空");
        }

        if (password.length() < 6 || password.length() > 20) {
            throw new StatusFailException("新密码长度应该为6~20位！");
        }

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