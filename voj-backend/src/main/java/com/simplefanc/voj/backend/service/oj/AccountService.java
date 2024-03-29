package com.simplefanc.voj.backend.service.oj;

import com.simplefanc.voj.backend.pojo.dto.ChangeEmailDTO;
import com.simplefanc.voj.backend.pojo.dto.ChangePasswordDTO;
import com.simplefanc.voj.backend.pojo.dto.CheckUsernameOrEmailDTO;
import com.simplefanc.voj.backend.pojo.vo.ChangeAccountVO;
import com.simplefanc.voj.backend.pojo.vo.CheckUsernameOrEmailVO;
import com.simplefanc.voj.backend.pojo.vo.UserHomeVO;
import com.simplefanc.voj.backend.pojo.vo.UserInfoVO;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 16:53
 * @Description:
 */
public interface AccountService {

    /**
     * @MethodName checkUsernameOrEmail
     * @Params * @param null
     * @Description 检验用户名和邮箱是否存在
     * @Return
     * @Since 2021/11/5
     */
    CheckUsernameOrEmailVO checkUsernameOrEmail(CheckUsernameOrEmailDTO checkUsernameOrEmailDTO);

    /**
     * @param uid
     * @MethodName getUserHomeInfo
     * @Description 前端userHome用户个人主页的数据请求，主要是返回解决题目数，AC的题目列表，提交总数，AC总数，Rating分，
     * @Return CommonResult
     * @Since 2021/01/07
     */
    UserHomeVO getUserHomeInfo(String uid, String username);

    /**
     * @MethodName changePassword
     * @Description 修改密码的操作，连续半小时内修改密码错误5次，则需要半个小时后才可以再次尝试修改密码
     * @Return
     * @Since 2021/1/8
     */
    ChangeAccountVO changePassword(ChangePasswordDTO changePasswordDTO);

    /**
     * @MethodName changeEmail
     * @Description 修改邮箱的操作，连续半小时内密码错误5次，则需要半个小时后才可以再次尝试修改
     * @Return
     * @Since 2021/1/9
     */
    ChangeAccountVO changeEmail(ChangeEmailDTO changeEmailDTO);

    UserInfoVO changeUserInfo(UserInfoVO userInfoVO);

}