package com.simplefanc.voj.backend.validator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.constants.TrainingEnum;
import com.simplefanc.voj.backend.common.exception.StatusAccessDeniedException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.dao.training.TrainingRegisterEntityService;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.common.pojo.entity.training.Training;
import com.simplefanc.voj.common.pojo.entity.training.TrainingRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/21 20:55
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class TrainingValidator {

    private final TrainingRegisterEntityService trainingRegisterEntityService;

    public void validateTrainingAuth(Training training) {

        // 是否为超级管理员
        boolean isRoot = UserSessionUtil.isRoot();
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        if (TrainingEnum.AUTH_PRIVATE.getValue().equals(training.getAuth())) {

            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("该训练属于私有题单，请先登录以校验权限！");
            }
            // 是否为该私有训练的创建者
            boolean isAuthor = training.getAuthor().equals(userRolesVo.getUsername());

            if (isRoot || isAuthor) {
                return;
            }

            // 如果三者都不是，需要做注册权限校验
            checkTrainingRegister(training.getId(), userRolesVo.getUid());
        }
    }

    private void checkTrainingRegister(Long tid, String uid) {
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

    public boolean isInTrainingOrAdmin(Training training, UserRolesVo userRolesVo) {
        if (TrainingEnum.AUTH_PRIVATE.getValue().equals(training.getAuth())) {
            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("该训练属于私有题单，请先登录以校验权限！");
            }
            // 是否为超级管理员
            boolean isRoot = UserSessionUtil.isRoot();
            // 是否为该私有训练的创建者
            boolean isAuthor = training.getAuthor().equals(userRolesVo.getUsername());

            if (isRoot || isAuthor) {
                return true;
            }

            // 如果三者都不是，需要做注册权限校验
            QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
            trainingRegisterQueryWrapper.eq("tid", training.getId());
            trainingRegisterQueryWrapper.eq("uid", userRolesVo.getUid());
            TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(trainingRegisterQueryWrapper,
                    false);

            return trainingRegister != null && trainingRegister.getStatus();

        }
        return true;
    }

}