package com.simplefanc.voj.server.controller.oj;

import com.simplefanc.voj.common.result.CommonResult;
import com.simplefanc.voj.server.pojo.dto.ChangeEmailDto;
import com.simplefanc.voj.server.pojo.dto.ChangePasswordDto;
import com.simplefanc.voj.server.pojo.dto.CheckUsernameOrEmailDto;
import com.simplefanc.voj.server.pojo.vo.ChangeAccountVo;
import com.simplefanc.voj.server.pojo.vo.CheckUsernameOrEmailVo;
import com.simplefanc.voj.server.pojo.vo.UserHomeVo;
import com.simplefanc.voj.server.pojo.vo.UserInfoVo;
import com.simplefanc.voj.server.service.oj.AccountService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: chenfan
 * @Date: 2020/10/23 12:00
 * @Description: 主要负责处理账号的相关操作
 */
@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * @MethodName checkUsernameOrEmail
     * @Description 检验用户名和邮箱是否存在
     * @Return
     * @Since 2020/11/5
     */
    @RequestMapping(value = "/check-username-or-email", method = RequestMethod.POST)
    public CommonResult<CheckUsernameOrEmailVo> checkUsernameOrEmail(@RequestBody CheckUsernameOrEmailDto checkUsernameOrEmailDto) {
        return CommonResult.successResponse(accountService.checkUsernameOrEmail(checkUsernameOrEmailDto));
    }

    /**
     * @param uid
     * @MethodName getUserHomeInfo
     * @Description 前端userHome用户个人主页的数据请求，主要是返回解决题目数，AC的题目列表，提交总数，AC总数，Rating分，
     * @Return CommonResult
     * @Since 2021/01/07
     */
    @GetMapping("/get-user-home-info")
    public CommonResult<UserHomeVo> getUserHomeInfo(@RequestParam(value = "uid", required = false) String uid,
                                                    @RequestParam(value = "username", required = false) String username) {
        return CommonResult.successResponse(accountService.getUserHomeInfo(uid, username));
    }


    /**
     * @MethodName changePassword
     * @Params * @param null
     * @Description 修改密码的操作，连续半小时内修改密码错误5次，则需要半个小时后才可以再次尝试修改密码
     * @Return
     * @Since 2021/1/8
     */

    @PostMapping("/change-password")
    @RequiresAuthentication
    public CommonResult<ChangeAccountVo> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        return CommonResult.successResponse(accountService.changePassword(changePasswordDto));
    }

    /**
     * @MethodName changeEmail
     * @Params * @param null
     * @Description 修改邮箱的操作，连续半小时内密码错误5次，则需要半个小时后才可以再次尝试修改
     * @Return
     * @Since 2021/1/9
     */
    @PostMapping("/change-email")
    @RequiresAuthentication
    public CommonResult<ChangeAccountVo> changeEmail(@RequestBody ChangeEmailDto changeEmailDto) {
        return CommonResult.successResponse(accountService.changeEmail(changeEmailDto));
    }

    @PostMapping("/change-userInfo")
    @RequiresAuthentication
    public CommonResult<UserInfoVo> changeUserInfo(@RequestBody UserInfoVo userInfoVo) {
        return CommonResult.successResponse(accountService.changeUserInfo(userInfoVo));
    }

}