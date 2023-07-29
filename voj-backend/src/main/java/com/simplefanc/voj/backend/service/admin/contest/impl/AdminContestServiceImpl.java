package com.simplefanc.voj.backend.service.admin.contest.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.exception.StatusSystemErrorException;
import com.simplefanc.voj.backend.dao.contest.ContestEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestRegisterEntityService;
import com.simplefanc.voj.backend.pojo.vo.AdminContestVO;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVO;
import com.simplefanc.voj.backend.service.admin.contest.AdminContestService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 11:20
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AdminContestServiceImpl implements AdminContestService {

    private final ContestEntityService contestEntityService;

    private final ContestRegisterEntityService contestRegisterEntityService;

    private final ContestValidator contestValidator;

    @Override
    public IPage<Contest> getContestList(Integer limit, Integer currentPage, String keyword) {

        if (currentPage == null || currentPage < 1) {
            currentPage = 1;
        }
        if (limit == null || limit < 1) {
            limit = 10;
        }
        IPage<Contest> iPage = new Page<>(currentPage, limit);
        QueryWrapper<Contest> queryWrapper = new QueryWrapper<>();
        // 过滤密码
        queryWrapper.select(Contest.class, info -> !"pwd".equals(info.getColumn()));
        if (!StrUtil.isEmpty(keyword)) {
            keyword = keyword.trim();
            queryWrapper.like("title", keyword).or().like("id", keyword);
        }
        queryWrapper.orderByAsc("status").orderByDesc("start_time");
        return contestEntityService.page(iPage, queryWrapper);
    }

    @Override
    public AdminContestVO getContest(Long cid) {
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);
        // 查询不存在
        if (contest == null) {
            throw new StatusFailException("查询失败：该比赛不存在,请检查参数cid是否准确！");
        }

        // 只有超级管理员和比赛拥有者才能操作
        if (!contestValidator.isContestAdmin(contest)) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }
        AdminContestVO adminContestVO = BeanUtil.copyProperties(contest, AdminContestVO.class, "starAccount");
        if (StrUtil.isEmpty(contest.getStarAccount())) {
            adminContestVO.setStarAccount(new ArrayList<>());
        } else {
            JSONObject jsonObject = JSONUtil.parseObj(contest.getStarAccount());
            List<String> starAccount = jsonObject.get("star_account", List.class);
            adminContestVO.setStarAccount(starAccount);
        }
        return adminContestVO;
    }

    @Override
    public void deleteContest(Long cid) {
        boolean isOk = contestEntityService.removeById(cid);
        // contest的id为其他表的外键的表中的对应数据都会被一起删除！
        // 删除成功
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }

    @Override
    public void addContest(AdminContestVO adminContestVO) {
        Contest contest = BeanUtil.copyProperties(adminContestVO, Contest.class, "starAccount");
        JSONObject accountJson = new JSONObject();
        accountJson.set("star_account", adminContestVO.getStarAccount());
        contest.setStarAccount(accountJson.toString());
        boolean isOk = contestEntityService.save(contest);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContest(AdminContestVO adminContestVO) {
        // 是否为超级管理员
        boolean isRoot = UserSessionUtil.isRoot();
        if (!isRoot && !contestValidator.isContestOwner(adminContestVO.getUid())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }
        Contest contest = BeanUtil.copyProperties(adminContestVO, Contest.class, "starAccount");
        JSONObject accountJson = new JSONObject();
        accountJson.set("star_account", adminContestVO.getStarAccount());
        contest.setStarAccount(accountJson.toString());
        Contest oldContest = contestEntityService.getById(contest.getId());
        boolean isOk = contestEntityService.saveOrUpdate(contest);
        if (isOk) {
            if (!contest.getAuth().equals(ContestEnum.AUTH_PUBLIC.getCode())) {
                // 改了比赛密码则需要删掉已有的注册比赛用户
                if (!Objects.equals(oldContest.getPwd(), contest.getPwd())) {
                    UpdateWrapper<ContestRegister> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("cid", contest.getId());
                    contestRegisterEntityService.remove(updateWrapper);
                }
            }
        } else {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public void changeContestVisible(Long cid, String uid, Boolean visible) {
        // 是否为超级管理员
        boolean isRoot = UserSessionUtil.isRoot();
        // 只有超级管理员和比赛拥有者才能操作
        if (!isRoot && !contestValidator.isContestOwner(uid)) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        boolean isOK = contestEntityService.saveOrUpdate(new Contest().setId(cid).setVisible(visible));

        if (!isOK) {
            throw new StatusFailException("修改失败");
        }
    }

    @Override
    public void cloneContest(Long cid) {
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusSystemErrorException("该比赛不存在，无法克隆！");
        }
        final UserRolesVO userInfo = UserSessionUtil.getUserInfo();
        contest.setUid(userInfo.getUid())
                .setAuthor(userInfo.getUsername())
                .setSource(cid.intValue())
                .setTitle(contest.getTitle() + " [Clone]");
        boolean isOk = contestEntityService.save(contest);
    }

}