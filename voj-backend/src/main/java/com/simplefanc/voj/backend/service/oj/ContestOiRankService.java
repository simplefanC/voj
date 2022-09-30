package com.simplefanc.voj.backend.service.oj;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestRegisterEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.pojo.vo.ContestRecordVo;
import com.simplefanc.voj.backend.pojo.vo.OIContestRankVo;
import com.simplefanc.voj.backend.pojo.vo.UserRolesVo;
import com.simplefanc.voj.backend.shiro.UserSessionUtil;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.constants.ContestConstant;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.user.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 20:11
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class ContestOiRankService {

    private final UserInfoEntityService userInfoEntityService;

    private final RedisUtil redisUtil;

    private final ContestRecordEntityService contestRecordEntityService;

    private final ContestRegisterEntityService contestRegisterEntityService;

    private final ContestValidator contestValidator;

    /**
     * @param isOpenSealRank
     * @param removeStarUser
     * @param concernedList
     * @param contest
     * @param currentPage
     * @param limit
     * @desc 获取OI比赛排行榜，有分页
     */
    public IPage<OIContestRankVo> getContestOiRankPage(Contest contest, Boolean isOpenSealRank, Boolean removeStarUser,
                                                       List<String> concernedList, String keyword,
                                                       Boolean useCache, Long cacheTime,
                                                       int currentPage, int limit) {

        List<OIContestRankVo> orderResultList = this.calculateOiRank(isOpenSealRank, removeStarUser,
                contest, concernedList, keyword, useCache, cacheTime);

        return getOiContestRankVoPage(currentPage, limit, orderResultList);
    }

    private Page<OIContestRankVo> getOiContestRankVoPage(int currentPage, int limit, List<OIContestRankVo> orderResultList) {
        // 计算好排行榜，然后进行分页
        Page<OIContestRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<OIContestRankVo> pageList = new ArrayList<>();
        // 计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(orderResultList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);
        return page;
    }

    /**
     * @param isOpenSealRank 是否是查询封榜后的数据
     * @param removeStar     是否需要移除打星队伍
     * @param contest        比赛实体信息
     * @param concernedList  关注的用户（uuid）列表
     * @param useCache       是否对初始排序计算的结果进行缓存
     * @param cacheTime      缓存的时间 单位秒
     * @MethodName calcOIRank
     * @Description
     * @Return
     * @Since 2021/12/10
     */
    public List<OIContestRankVo> calculateOiRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                 List<String> concernedList, String keyword, boolean useCache, Long cacheTime) {
        List<OIContestRankVo> orderResultList;
        if (useCache) {
            String key = ContestConstant.CONTEST_RANK_CAL_RESULT_CACHE + "_" + contest.getId();
            orderResultList = (List<OIContestRankVo>) redisUtil.get(key);
            if (orderResultList == null) {
                orderResultList = getOiOrderRank(contest, isOpenSealRank);
                redisUtil.set(key, orderResultList, cacheTime);
            }
        } else {
            orderResultList = getOiOrderRank(contest, isOpenSealRank);
        }
        // 记录当前用户排名数据和关注列表的用户排名数据
        List<OIContestRankVo> topOiRankVoList = new ArrayList<>();
        // 设置 rank 和 添加置顶
        computeOiRankNo(contest, concernedList, removeStar, orderResultList, topOiRankVoList);
        topOiRankVoList.addAll(orderResultList);
        if(!StrUtil.isEmpty(keyword)) {
            return topOiRankVoList.stream()
                    .filter(rankVo -> filterKeyword(rankVo, keyword))
                    .collect(Collectors.toList());
        }
        return topOiRankVoList;
    }

    boolean filterKeyword(OIContestRankVo rankVo, String keyword){
        final String realname = rankVo.getRealname();
        if(!StrUtil.isEmpty(realname)){
            if(realname.contains(keyword)) {
                return true;
            }
        }
        final String username = rankVo.getUsername();
        if(!StrUtil.isEmpty(username)){
            if(username.contains(keyword)) {
                return true;
            }
        }
        final String school = rankVo.getSchool();
        if(!StrUtil.isEmpty(school)){
            if(school.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private List<OIContestRankVo> getOiOrderRank(Contest contest, Boolean isOpenSealRank) {
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        List<String> contestAdminUidList = new ArrayList<>(superAdminUidList);
        contestAdminUidList.add(contest.getUid());

        final List<ContestRecordVo> oiContestRecordList = contestRecordEntityService.getOIContestRecord(contest, isOpenSealRank)
                .stream()
                .filter(contestRecord -> contest.getContestAdminRank() || !contestAdminUidList.contains(contestRecord.getUid()))
                .collect(Collectors.toList());
        Set<String> hasRecordUserNameSet = oiContestRecordList.stream()
                .map(ContestRecordVo::getUsername)
                .collect(Collectors.toSet());

        Map<String, OIContestRankVo> uidContestRankVoMap = initOiContestRankVo(hasRecordUserNameSet);

        List<OIContestRankVo> result = new ArrayList<>(uidContestRankVoMap.values());

        boolean isHighestRankScore = ContestConstant.OI_RANK_HIGHEST_SCORE.equals(contest.getOiRankScoreType());
        oiContestRecordList.forEach(contestRecord ->
                setSubmissionInfo(contestRecord, uidContestRankVoMap.get(contestRecord.getUid()), isHighestRankScore));

        HashMap<String, Map<String, Integer>> uidTimeInfoMap = getUidTimeInfoMap(oiContestRecordList);
        setTimeInfoToResult(result, uidTimeInfoMap);

        // 根据总得分进行降序，再根据总时耗升序排序
        result = result.stream()
                .sorted(Comparator.comparing(OIContestRankVo::getTotalScore, Comparator.reverseOrder())
                        .thenComparing(OIContestRankVo::getTotalTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());

        if (contestValidator.isContestAdmin(contest)) {
            result.addAll(getNoRecordUserOiContestRankVos(contest, hasRecordUserNameSet));
        }
        return result;
    }

    private void computeOiRankNo(Contest contest, List<String> concernedList, boolean removeStar, List<OIContestRankVo> orderResultList, List<OIContestRankVo> topOiRankVoList) {
        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());
        // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
        if (removeStar) {
            orderResultList.removeIf(contestRankVo -> starAccountMap.containsKey(contestRankVo.getUsername()));
        }
        String currentUserId = null;
        final UserRolesVo userInfo = UserSessionUtil.getUserInfo();
        // 外榜：可能未登录
        if(userInfo != null) {
            currentUserId = userInfo.getUid();
        }
        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            concernedList.remove(currentUserId);
        }

        int rankNum = 1;
        OIContestRankVo preOIRankVo = null;
        for (int i = 0; i < orderResultList.size(); i++) {
            OIContestRankVo contestRankVo = orderResultList.get(i);
            contestRankVo.setSeq(i + 1);
            // 设置 rank
            if (starAccountMap.containsKey(contestRankVo.getUsername())) {
                // 打星队伍排名为-1
                contestRankVo.setRank(-1);
            } else {
                if (rankNum == 1) {
                    contestRankVo.setRank(rankNum);
                } else {
                    // 当前用户的程序总运行时间和总得分跟前一个用户一样，同时前一个不应该为打星用户，排名则一样
                    if (preOIRankVo.getTotalScore().equals(contestRankVo.getTotalScore())
                            && preOIRankVo.getTotalTime().equals(contestRankVo.getTotalTime())) {
                        contestRankVo.setRank(preOIRankVo.getRank());
                    } else {
                        contestRankVo.setRank(rankNum);
                    }
                }
                preOIRankVo = contestRankVo;
                rankNum++;
            }

            // 添加自己
            if (StrUtil.isNotEmpty(currentUserId) && currentUserId.equals(contestRankVo.getUid())) {
                topOiRankVoList.add(contestRankVo);
            }

            // 添加关注用户
            if (needAddConcernedUser && concernedList.contains(contestRankVo.getUid())) {
                topOiRankVoList.add(contestRankVo);
            }
        }
    }

    private List<UserInfo> getNoRecordUserInfos(Contest contest, Set<String> hasRecordUserNameSet) {
        String extra = ReUtil.getGroup1("<extra>([\\S\\s]*?)<\\/extra>", contest.getAccountLimitRule());
        if (StrUtil.isBlank(extra)) {
            return new ArrayList<>();
        }
        final Set<String> extraUserNameSet = Arrays.stream(extra.split("\n"))
                .filter(u -> !hasRecordUserNameSet.contains(u))
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(extraUserNameSet)) {
            return new ArrayList<>();
        }
        return userInfoEntityService.lambdaQuery()
                .in(UserInfo::getUsername, extraUserNameSet)
                .list();
    }

    /**
     * 没有提交记录的用户 OIContestRankVo
     * @param contest
     * @param hasRecordUserNameSet
     * @return
     */
    private List<OIContestRankVo> getNoRecordUserOiContestRankVos(Contest contest, Set<String> hasRecordUserNameSet) {
        final Set<String> registeredUsers = contestRegisterEntityService.getRegisteredUsers(contest.getId());
        final List<UserInfo> noRecordUserInfos = getNoRecordUserInfos(contest, hasRecordUserNameSet);
        return noRecordUserInfos.stream()
                .map(this::initOiContestRankVo)
                .map(contestRankVo -> contestRankVo.setRegistered(registeredUsers.contains(contestRankVo.getUid())))
                .sorted(Comparator.comparing(OIContestRankVo::getRegistered, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private Map<String, OIContestRankVo> initOiContestRankVo(Set<String> userNameSet) {
        if (CollUtil.isEmpty(userNameSet)) {
            return new HashMap<>();
        }

        return userInfoEntityService.lambdaQuery()
                .in(UserInfo::getUsername, userNameSet)
                .list()
                .stream()
                .map(this::initOiContestRankVo)
                .collect(Collectors.toMap(OIContestRankVo::getUid, o -> o));
    }

    private OIContestRankVo initOiContestRankVo(UserInfo userInfo) {
        return new OIContestRankVo()
                .setRealname(userInfo.getRealname())
                .setUid(userInfo.getUuid())
                .setUsername(userInfo.getUsername())
                .setSchool(userInfo.getSchool())
                .setAvatar(userInfo.getAvatar())
                .setGender(userInfo.getGender())
                .setNickname(userInfo.getNickname())
                .setTotalScore(0)
                .setTotalTime(0)
                .setSubmissionInfo(new HashMap<>());
    }

    /**
     * "totalScore": 400,
     * "submissionInfo": {
     * "1": 100,
     * "2": 100,
     * "3": 100,
     * "4": 100
     * }
     */
    private void setSubmissionInfo(ContestRecordVo contestRecord, OIContestRankVo oiContestRankVo, boolean isHighestRankScore) {
        Map<String, Integer> submissionInfo = oiContestRankVo.getSubmissionInfo();
        Integer score = submissionInfo.get(contestRecord.getDisplayId());
        if (isHighestRankScore) {
            if (score == null) {
                oiContestRankVo.setTotalScore(oiContestRankVo.getTotalScore() + contestRecord.getScore());
                submissionInfo.put(contestRecord.getDisplayId(), contestRecord.getScore());
            }
        } else {
            if (contestRecord.getScore() != null) {
                // 为了避免同个提交时间的重复计算
                if (score != null) {
                    oiContestRankVo.setTotalScore(oiContestRankVo.getTotalScore() - score + contestRecord.getScore());
                } else {
                    oiContestRankVo.setTotalScore(oiContestRankVo.getTotalScore() + contestRecord.getScore());
                }
            }
            submissionInfo.put(contestRecord.getDisplayId(), contestRecord.getScore());
        }
    }

    /**
     * uid -> timeInfo
     *
     * @param oiContestRecord
     * @return
     */
    private HashMap<String, Map<String, Integer>> getUidTimeInfoMap(List<ContestRecordVo> oiContestRecord) {
        HashMap<String, Map<String, Integer>> uidTimeInfoMap = new HashMap<>();
        oiContestRecord.stream()
                .filter(contestRecord -> Objects.equals(contestRecord.getStatus(), ContestEnum.RECORD_AC.getCode()))
                .forEach(contestRecord -> computeUidTimeInfoMap(uidTimeInfoMap, contestRecord));

        return uidTimeInfoMap;
    }

    /**
     * uid,pid,cpid,MAX(score)
     * 相同分数，不同时间，e.g.
     * 1,7772,311,100,6
     * 1,7772,311,100,10
     *
     * @param uidTimeInfoMap
     * @param contestRecord
     */
    private void computeUidTimeInfoMap(HashMap<String, Map<String, Integer>> uidTimeInfoMap, ContestRecordVo contestRecord) {
        Map<String, Integer> pidTimeMap = uidTimeInfoMap.get(contestRecord.getUid());
        if (pidTimeMap != null) {
            Integer useTime = pidTimeMap.get(contestRecord.getDisplayId());
            if (useTime != null) {
                // 如果时间消耗比原来的少
                if (useTime > contestRecord.getUseTime()) {
                    pidTimeMap.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                }
            } else {
                pidTimeMap.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
            }
        } else {
            uidTimeInfoMap.put(contestRecord.getUid(),
                    MapUtil.builder(new HashMap<String, Integer>())
                            .put(contestRecord.getDisplayId(), contestRecord.getUseTime()).build());
        }
    }

    /**
     * "totalTime": 11,
     * "timeInfo": {
     * "1": 3,
     * "2": 3,
     * "3": 3,
     * "4": 2
     * }
     *
     * @param result
     * @param uidMapTime
     */
    private void setTimeInfoToResult(List<OIContestRankVo> result, HashMap<String, Map<String, Integer>> uidMapTime) {
        result.forEach(oiContestRankVo -> {
            Map<String, Integer> pidMapTime = uidMapTime.get(oiContestRankVo.getUid());
            int sumTime = Optional.ofNullable(pidMapTime)
                    .map(val -> val.values().stream().reduce(0, Integer::sum))
                    .orElse(0);
            oiContestRankVo.setTotalTime(sumTime);
            oiContestRankVo.setTimeInfo(pidMapTime);
        });
    }

    private HashMap<String, Boolean> starAccountToMap(String starAccountStr) {
        if (StrUtil.isEmpty(starAccountStr)) {
            return new HashMap<>();
        }
        JSONObject jsonObject = JSONUtil.parseObj(starAccountStr);
        List<String> list = jsonObject.get("star_account", List.class);
        HashMap<String, Boolean> res = new HashMap<>();
        for (String str : list) {
            if (!StrUtil.isEmpty(str)) {
                res.put(str, true);
            }
        }
        return res;
    }

}