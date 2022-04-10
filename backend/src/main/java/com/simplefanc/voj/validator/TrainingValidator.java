package com.simplefanc.voj.validator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.common.exception.StatusAccessDeniedException;
import com.simplefanc.voj.common.exception.StatusForbiddenException;
import com.simplefanc.voj.dao.training.TrainingRegisterEntityService;
import com.simplefanc.voj.pojo.entity.training.Training;
import com.simplefanc.voj.pojo.entity.training.TrainingRegister;
import com.simplefanc.voj.pojo.vo.UserRolesVo;
import com.simplefanc.voj.utils.Constants;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/21 20:55
 * @Description:
 */
@Component
public class TrainingValidator {

    @Resource
    private TrainingRegisterEntityService trainingRegisterEntityService;

    public void validateTrainingAuth(Training training) throws StatusAccessDeniedException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        validateTrainingAuth(training, userRolesVo);
    }


    public void validateTrainingAuth(Training training, UserRolesVo userRolesVo) throws StatusAccessDeniedException, StatusForbiddenException {

        boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        if (Constants.Training.AUTH_PRIVATE.getValue().equals(training.getAuth())) {

            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("该训练属于私有题单，请先登录以校验权限！");
            }

            boolean isAuthor = training.getAuthor().equals(userRolesVo.getUsername()); // 是否为该私有训练的创建者

            if (isRoot || isAuthor) {
                return;
            }

            // 如果三者都不是，需要做注册权限校验
            checkTrainingRegister(training.getId(), userRolesVo.getUid());
        }
    }

    private void checkTrainingRegister(Long tid, String uid) throws StatusAccessDeniedException, StatusForbiddenException {
        QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
        trainingRegisterQueryWrapper.eq("tid", tid);
        trainingRegisterQueryWrapper.eq("uid", uid);
        TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(trainingRegisterQueryWrapper, false);

        if (trainingRegister == null) {
            throw new StatusAccessDeniedException("该训练属于私有，请先使用专属密码注册！");
        }

        if (!trainingRegister.getStatus()) {
            throw new StatusForbiddenException("错误：你已被禁止参加该训练！");
        }
    }

    public boolean isInTrainingOrAdmin(Training training, UserRolesVo userRolesVo) throws StatusAccessDeniedException {
        if (Constants.Training.AUTH_PRIVATE.getValue().equals(training.getAuth())) {
            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("该训练属于私有题单，请先登录以校验权限！");
            }
            boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员
            boolean isAuthor = training.getAuthor().equals(userRolesVo.getUsername()); // 是否为该私有训练的创建者


            if (isRoot || isAuthor) {
                return true;
            }

            // 如果三者都不是，需要做注册权限校验
            QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
            trainingRegisterQueryWrapper.eq("tid", training.getId());
            trainingRegisterQueryWrapper.eq("uid", userRolesVo.getUid());
            TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(trainingRegisterQueryWrapper, false);

            return trainingRegister != null && trainingRegister.getStatus();

        }
        return true;
    }
}