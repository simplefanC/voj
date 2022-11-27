package com.simplefanc.voj.backend.controller.oj;

import com.simplefanc.voj.backend.pojo.dto.ChangeEmailDTO;
import com.simplefanc.voj.backend.pojo.dto.ChangePasswordDTO;
import com.simplefanc.voj.backend.pojo.dto.CheckUsernameOrEmailDTO;
import com.simplefanc.voj.backend.pojo.vo.ChangeAccountVO;
import com.simplefanc.voj.backend.pojo.vo.CheckUsernameOrEmailVO;
import com.simplefanc.voj.backend.pojo.vo.UserHomeVO;
import com.simplefanc.voj.backend.pojo.vo.UserInfoVO;
import com.simplefanc.voj.backend.service.oj.AccountService;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: chenfan
 * @Date: 2021/10/23 12:00
 * @Description: 主要负责处理账号的相关操作
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * @MethodName checkUsernameOrEmail
     * @Description 检验用户名和邮箱是否存在
     * @Return
     * @Since 2021/11/5
     */
    @RequestMapping(value = "/check-username-or-email", method = RequestMethod.POST)
    public CommonResult<CheckUsernameOrEmailVO> checkUsernameOrEmail(
            @RequestBody CheckUsernameOrEmailDTO checkUsernameOrEmailDTO) {
        return CommonResult.successResponse(accountService.checkUsernameOrEmail(checkUsernameOrEmailDTO));
    }

    /**
     * @param uid
     * @MethodName getUserHomeInfo
     * @Description 前端userHome用户个人主页的数据请求，主要是返回解决题目数，AC的题目列表，提交总数，AC总数，Rating分，
     * @Return CommonResult
     * @Since 2021/01/07
     */
    @GetMapping("/get-user-home-info")
    public CommonResult<UserHomeVO> getUserHomeInfo(@RequestParam(value = "uid", required = false) String uid,
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
    public CommonResult<ChangeAccountVO> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        return CommonResult.successResponse(accountService.changePassword(changePasswordDTO));
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
    public CommonResult<ChangeAccountVO> changeEmail(@RequestBody ChangeEmailDTO changeEmailDTO) {
        return CommonResult.successResponse(accountService.changeEmail(changeEmailDTO));
    }

    @PostMapping("/change-userInfo")
    @RequiresAuthentication
    public CommonResult<UserInfoVO> changeUserInfo(@RequestBody UserInfoVO userInfoVO) {
        return CommonResult.successResponse(accountService.changeUserInfo(userInfoVO));
    }

}