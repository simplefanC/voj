package com.simplefanc.voj.backend.service.oj.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.common.exception.StatusForbiddenException;
import com.simplefanc.voj.backend.common.exception.StatusNotFoundException;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.common.AnnouncementEntityService;
import com.simplefanc.voj.backend.dao.contest.*;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.problem.*;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.pojo.dto.ContestPrintDto;
import com.simplefanc.voj.backend.pojo.dto.ContestRankDto;
import com.simplefanc.voj.backend.pojo.dto.RegisterContestDto;
import com.simplefanc.voj.backend.pojo.dto.UserReadContestAnnouncementDto;
import com.simplefanc.voj.backend.pojo.vo.*;
import com.simplefanc.voj.backend.service.oj.ContestAcmRankService;
import com.simplefanc.voj.backend.service.oj.ContestOiRankService;
import com.simplefanc.voj.backend.service.oj.ContestService;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.constants.ProblemEnum;
import com.simplefanc.voj.common.constants.RedisConstant;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;
import com.simplefanc.voj.common.pojo.entity.contest.*;
import com.simplefanc.voj.common.pojo.entity.problem.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 22:26
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class ContestServiceImpl implements ContestService {

    private final ContestEntityService contestEntityService;

    private final ContestProblemEntityService contestProblemEntityService;

    private final ContestAnnouncementEntityService contestAnnouncementEntityService;

    private final AnnouncementEntityService announcementEntityService;

    private final ContestRegisterEntityService contestRegisterEntityService;

    private final ProblemEntityService problemEntityService;

    private final ProblemTagEntityService problemTagEntityService;

    private final TagEntityService tagEntityService;

    private final LanguageEntityService languageEntityService;

    private final ProblemLanguageEntityService problemLanguageEntityService;

    private final JudgeEntityService judgeEntityService;

    private final CodeTemplateEntityService codeTemplateEntityService;

    private final ContestPrintEntityService contestPrintEntityService;

    private final UserInfoEntityService userInfoEntityService;

    private final RedisUtil redisUtil;

    private final ContestValidator contestValidator;

    private final ContestAcmRankService contestAcmRankService;

    private final ContestOiRankService contestOiRankService;

    @Override
    public IPage<ContestVo> getContestList(Integer limit, Integer currentPage, Integer status, Integer type,
                                           String keyword) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;
        return contestEntityService.getContestList(limit, currentPage, type, status, keyword);
    }

    @Override
    public ContestVo getContestInfo(Long cid) {

        ContestVo contestInfo = contestEntityService.getContestInfoById(cid);
        if (contestInfo == null) {
            throw new StatusFailException("对不起，该比赛不存在!");
        }
        // 设置当前服务器系统时间
        contestInfo.setNow(new Date());

        return contestInfo;
    }

    @Override
    public void toRegisterContest(RegisterContestDto registerContestDto) {

        Long cid = registerContestDto.getCid();
        String password = registerContestDto.getPassword();
        if (cid == null) {
            throw new StatusFailException("cid不能为空！");
        }

        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();
        Contest contest = contestEntityService.getById(cid);

        if (contest == null || !contest.getVisible()) {
            throw new StatusFailException("对不起，该比赛不存在!");
        }

        // 密码不对
        if (contest.getOpenPwdLimit() && !contest.getPwd().equals(password)) {
            throw new StatusFailException("比赛密码错误，请重新输入！");
        }

        // 需要校验当前比赛是否开启账号规则限制，如果有，需要对当前用户的用户名进行验证
        if (contest.getOpenAccountLimit()
                && !contestValidator.validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
            throw new StatusFailException("对不起！本次比赛只允许特定账号规则的用户参赛！");
        }

        QueryWrapper<ContestRegister> wrapper = new QueryWrapper<ContestRegister>().eq("cid", cid).eq("uid",
                userRolesVo.getUid());
        if (contestRegisterEntityService.getOne(wrapper, false) != null) {
            throw new StatusFailException("您已注册过该比赛，请勿重复注册！");
        }

        boolean isOk = contestRegisterEntityService
                .saveOrUpdate(new ContestRegister().setCid(cid).setUid(userRolesVo.getUid()));

        if (!isOk) {
            throw new StatusFailException("校验比赛权限失败，请稍后再试");
        }
    }

    @Override
    public AccessVo getContestAccess(Long cid) {
        // 获取当前登录的用户
        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        ContestRegister contestRegister = contestRegisterEntityService.getOne(
                new QueryWrapper<ContestRegister>()
                        .eq("cid", cid)
                        .eq("uid", userRolesVo.getUid()), false);

        Contest contest = contestEntityService.getById(cid);
        if (contest == null || !contest.getVisible()) {
            throw new StatusFailException("对不起，该比赛不存在!");
        }

        boolean access = false;
        if (contestRegister != null) {
            access = true;
            if (contest.getOpenAccountLimit()
                    && !contestValidator.validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                access = false;
                contestRegisterEntityService.removeById(contestRegister.getId());
            }
        } else {
            if (contest.getOpenAccountLimit()
                    && contestValidator.validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                access = true;

                boolean isOk = contestRegisterEntityService
                        .save(new ContestRegister().setCid(cid).setUid(userRolesVo.getUid()));

                if (!isOk) {
                    throw new StatusFailException("校验比赛权限失败，请稍后再试");
                }
            }
        }

        AccessVo accessVo = new AccessVo();
        accessVo.setAccess(access);
        return accessVo;
    }

    @Override
    public List<ContestProblemVo> getContestProblem(Long cid) {
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目列表，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest);

        boolean isAdmin = contestValidator.isContestAdmin(contest);
        // 如果比赛开启封榜
        if (contestValidator.isOpenSealRank(contest, true)) {
            return contestProblemEntityService.getContestProblemList(cid, contest.getStartTime(),
                    contest.getEndTime(), contest.getSealRankTime(), isAdmin, contest.getAuthor())
                    .stream().sorted().collect(Collectors.toList());
        }
        return contestProblemEntityService.getContestProblemList(cid, contest.getStartTime(),
                contest.getEndTime(), null, isAdmin, contest.getAuthor())
                .stream().sorted().collect(Collectors.toList());
    }

    @Override
    // TODO 行数过多
    public ProblemInfoVo getContestProblemDetails(Long cid, String displayId) {

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);
        contestValidator.validateContestAuth(contest);

        // 根据cid和displayId获取pid
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).eq("display_id", displayId);
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper);

        if (contestProblem == null) {
            throw new StatusNotFoundException("该比赛题目不存在");
        }

        // 查询题目详情，题目标签，题目语言，题目做题情况
        Problem problem = problemEntityService.getById(contestProblem.getPid());

        if (problem.getAuth().equals(ProblemEnum.AUTH_PRIVATE.getCode())) {
            throw new StatusForbiddenException("该比赛题目当前不可访问！");
        }

        // 设置比赛题目的标题为设置展示标题
        problem.setTitle(contestProblem.getDisplayTitle());

        List<Tag> tags = new LinkedList<>();

        // 比赛结束后才开放标签和source、出题人、难度
        if (contest.getStatus().intValue() != ContestEnum.STATUS_ENDED.getCode()) {
            problem.setSource(null);
            problem.setAuthor(null);
            problem.setDifficulty(null);
            QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
            problemTagQueryWrapper.eq("pid", contestProblem.getPid());
            // 获取该题号对应的标签id
            List<Long> tidList = new LinkedList<>();
            problemTagEntityService.list(problemTagQueryWrapper).forEach(problemTag -> {
                tidList.add(problemTag.getTid());
            });
            if (tidList.size() != 0) {
                tags = (List<Tag>) tagEntityService.listByIds(tidList);
            }
        }
        // 记录 languageId对应的name
        HashMap<Long, String> tmpMap = new HashMap<>();

        // 获取题目提交的代码支持的语言
        List<String> languagesStr = new LinkedList<>();
        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", contestProblem.getPid()).select("lid");
        List<Long> lidList = problemLanguageEntityService.list(problemLanguageQueryWrapper).stream()
                .map(ProblemLanguage::getLid).collect(Collectors.toList());
        languageEntityService.listByIds(lidList).forEach(language -> {
            languagesStr.add(language.getName());
            tmpMap.put(language.getId(), language.getName());
        });

        Date sealRankTime = null;
        // 封榜时间除超级管理员和比赛管理员外 其它人不可看到最新数据
        if (contestValidator.isOpenSealRank(contest, true)) {
            sealRankTime = contest.getSealRankTime();
        }

        // 筛去 比赛管理员和超级管理员的提交
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        superAdminUidList.add(contest.getUid());

        // 获取题目的提交记录
        ProblemCountVo problemCount = judgeEntityService.getContestProblemCount(contestProblem.getPid(),
                contestProblem.getId(), contestProblem.getCid(), contest.getStartTime(), sealRankTime,
                superAdminUidList);

        // 获取题目的代码模板
        QueryWrapper<CodeTemplate> codeTemplateQueryWrapper = new QueryWrapper<>();
        codeTemplateQueryWrapper.eq("pid", problem.getId()).eq("status", true);
        List<CodeTemplate> codeTemplates = codeTemplateEntityService.list(codeTemplateQueryWrapper);
        HashMap<String, String> langNameAndCode = new HashMap<>();
        if (codeTemplates.size() > 0) {
            for (CodeTemplate codeTemplate : codeTemplates) {
                langNameAndCode.put(tmpMap.get(codeTemplate.getLid()), codeTemplate.getCode());
            }
        }
        // 将数据统一写入到一个Vo返回数据实体类中
        return new ProblemInfoVo(problem, tags, languagesStr, problemCount, langNameAndCode);
    }

    // TODO 参数过多
    @Override
    public IPage<JudgeVo> getContestSubmissionList(Integer limit, Integer currentPage, Boolean onlyMine,
                                                   String displayId, Integer searchStatus, String searchUsername, Long searchCid, Boolean beforeContestSubmit,
                                                   Boolean completeProblemId) {

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(searchCid);
        contestValidator.validateContestAuth(contest);

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        String uid = null;
        // 只查看当前用户的提交
        final String userId = UserSessionUtil.getUserInfo().getUid();
        if (onlyMine) {
            uid = userId;
        }

        String contestType;
        if (contest.getType().intValue() == ContestEnum.TYPE_ACM.getCode()) {
            contestType = ContestEnum.TYPE_ACM.getName();
        } else {
            contestType = ContestEnum.TYPE_OI.getName();
        }

        Date sealRankTime = null;
        // 需要判断是否需要封榜
        if (contestValidator.isOpenSealRank(contest, true)) {
            sealRankTime = contest.getSealRankTime();
        }

        // OI比赛封榜期间不更新; ACM比赛封榜期间可看到自己的提交(sealTimeUid)，但是其它人的不可见
        IPage<JudgeVo> contestJudgeList = judgeEntityService.getContestJudgeList(limit, currentPage, displayId,
                searchCid, searchStatus, searchUsername, uid, beforeContestSubmit, contestType, contest.getStartTime(),
                sealRankTime, userId, completeProblemId);

        // 未查询到一条数据
        if (contestJudgeList.getTotal() == 0) {
            return contestJudgeList;
        }
        // 比赛还是进行阶段，同时不是超级管理员与比赛管理员，需要将除自己之外的提交的时间、空间、长度隐藏
        if (contest.getStatus().intValue() == ContestEnum.STATUS_RUNNING.getCode() && !contestValidator.isContestAdmin(contest)) {
            contestJudgeList.getRecords().forEach(judgeVo -> {
                if (!judgeVo.getUid().equals(userId)) {
                    judgeVo.setTime(null);
                    judgeVo.setMemory(null);
                    judgeVo.setLength(null);
                }
            });
        }
        return contestJudgeList;
    }

    @Override
    public IPage getContestRank(ContestRankDto contestRankDto) {

        Long cid = contestRankDto.getCid();
        List<String> concernedList = contestRankDto.getConcernedList();
        Integer currentPage = contestRankDto.getCurrentPage();
        Integer limit = contestRankDto.getLimit();
        Boolean removeStarUser = contestRankDto.getRemoveStar();
        Boolean forceRefresh = contestRankDto.getForceRefresh();
        final String keyword = contestRankDto.getKeyword();

        if (cid == null) {
            throw new StatusFailException("错误：cid不能为空");
        }
        if (removeStarUser == null) {
            removeStarUser = false;
        }
        if (forceRefresh == null) {
            forceRefresh = false;
        }
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(contestRankDto.getCid());
        contestValidator.validateContestAuth(contest);

        // 校验该比赛是否开启了封榜模式，超级管理员和比赛创建者可以直接看到实际榜单
        boolean isOpenSealRank = contestValidator.isOpenSealRank(contest, forceRefresh);

        if (contest.getType().intValue() == ContestEnum.TYPE_ACM.getCode()) {
            // ACM比赛
            // 进行排行榜计算以及排名分页
            return contestAcmRankService.getContestAcmRankPage(contest, isOpenSealRank, removeStarUser,
                    concernedList, keyword, false, null, currentPage, limit);

        } else {
            // OI比赛
            return contestOiRankService.getContestOiRankPage(contest, isOpenSealRank, removeStarUser,
                    concernedList, keyword, false, null, currentPage, limit);
        }
    }

    @Override
    public Set<String> getContestAdminUidList(Contest contest) {
        List<String> contestAdminUidList = userInfoEntityService.getSuperAdminUidList();
        contestAdminUidList.add(contest.getUid());
        return new HashSet<>(contestAdminUidList);
    }

    @Override
    public IPage<AnnouncementVo> getContestAnnouncement(Long cid, Integer limit, Integer currentPage) {
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest);

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;

        return announcementEntityService.getContestAnnouncement(cid, true, limit, currentPage);
    }

    @Override
    public List<Announcement> getContestUserNotReadAnnouncement(
            UserReadContestAnnouncementDto userReadContestAnnouncementDto) {

        Long cid = userReadContestAnnouncementDto.getCid();
        List<Long> readAnnouncementList = userReadContestAnnouncementDto.getReadAnnouncementList();

        QueryWrapper<ContestAnnouncement> contestAnnouncementQueryWrapper = new QueryWrapper<>();
        contestAnnouncementQueryWrapper.eq("cid", cid);
        if (readAnnouncementList != null && readAnnouncementList.size() > 0) {
            contestAnnouncementQueryWrapper.notIn("aid", readAnnouncementList);
        }
        List<ContestAnnouncement> announcementList = contestAnnouncementEntityService
                .list(contestAnnouncementQueryWrapper);

        List<Long> aidList = announcementList.stream()
                .map(ContestAnnouncement::getAid)
                .collect(Collectors.toList());

        if (aidList.size() > 0) {
            QueryWrapper<Announcement> announcementQueryWrapper = new QueryWrapper<>();
            announcementQueryWrapper.in("id", aidList).orderByDesc("gmt_create");
            return announcementEntityService.list(announcementQueryWrapper);
        } else {
            return new ArrayList<>();
        }

    }

    @Override
    public void submitPrintText(ContestPrintDto contestPrintDto) {

        UserRolesVo userRolesVo = UserSessionUtil.getUserInfo();

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(contestPrintDto.getCid());
        contestValidator.validateContestAuth(contest);

        String lockKey = RedisConstant.CONTEST_ADD_PRINT_LOCK + userRolesVo.getUid();
        if (redisUtil.hasKey(lockKey)) {
            long expire = redisUtil.getExpire(lockKey);
            throw new StatusForbiddenException("提交打印功能限制，请在" + expire + "秒后再进行提交！");
        } else {
            redisUtil.set(lockKey, 1, 30);
        }

        boolean isOk = contestPrintEntityService.saveOrUpdate(
                new ContestPrint().setCid(contestPrintDto.getCid()).setContent(contestPrintDto.getContent())
                        .setUsername(userRolesVo.getUsername()).setRealname(userRolesVo.getRealname()));

        if (!isOk) {
            throw new StatusFailException("提交失败");
        }

    }

}