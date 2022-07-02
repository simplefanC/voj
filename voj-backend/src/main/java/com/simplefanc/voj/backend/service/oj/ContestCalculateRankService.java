package com.simplefanc.voj.backend.service.oj;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.contest.ContestRecordEntityService;
import com.simplefanc.voj.backend.dao.contest.ContestRegisterEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.pojo.vo.ACMContestRankVo;
import com.simplefanc.voj.backend.pojo.vo.ContestRecordVo;
import com.simplefanc.voj.backend.pojo.vo.OIContestRankVo;
import com.simplefanc.voj.backend.validator.ContestValidator;
import com.simplefanc.voj.common.constants.ContestConstant;
import com.simplefanc.voj.common.constants.ContestEnum;
import com.simplefanc.voj.common.pojo.entity.contest.Contest;
import com.simplefanc.voj.common.pojo.entity.contest.ContestRegister;
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
public class ContestCalculateRankService {

    private final UserInfoEntityService userInfoEntityService;

    private final RedisUtil redisUtil;

    private final ContestRecordEntityService contestRecordEntityService;

    private final ContestRegisterEntityService contestRegisterEntityService;

    private final ContestValidator contestValidator;

    public List<ACMContestRankVo> calculateAcmRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                   String currentUserId, List<String> concernedList) {
        return calculateAcmRank(isOpenSealRank, removeStar, contest, currentUserId, concernedList, false, null);
    }

    public List<OIContestRankVo> calculateOiRank(Boolean isOpenSealRank, Boolean removeStar, Contest contest,
                                                 String currentUserId, List<String> concernedList) {

        return calculateOiRank(isOpenSealRank, removeStar, contest, currentUserId, concernedList, false, null);
    }

    /**
     * @param isOpenSealRank 是否是查询封榜后的数据
     * @param removeStar     是否需要移除打星队伍
     * @param contest        比赛实体信息
     * @param currentUserId  当前查看榜单的用户uuid,不为空则将该数据复制一份放置列表最前
     * @param concernedList  关注的用户（uuid）列表
     * @param useCache       是否对初始排序计算的结果进行缓存
     * @param cacheTime      缓存的时间 单位秒
     * @MethodName calcACMRank
     * @Description
     * @Return
     * @Since 2021/12/10
     */
    public List<ACMContestRankVo> calculateAcmRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                   String currentUserId, List<String> concernedList, boolean useCache, Long cacheTime) {

        List<ACMContestRankVo> orderResultList;
        if (useCache) {
            String key = ContestConstant.CONTEST_RANK_CAL_RESULT_CACHE + "_" + contest.getId();
            orderResultList = (List<ACMContestRankVo>) redisUtil.get(key);
            if (orderResultList == null) {
                orderResultList = getAcmOrderRank(contest, isOpenSealRank);
                redisUtil.set(key, orderResultList, cacheTime);
            }
        } else {
            orderResultList = getAcmOrderRank(contest, isOpenSealRank);
        }
        // 记录当前用户排名数据和关注列表的用户排名数据
        List<ACMContestRankVo> topAcmRankVoList = new ArrayList<>();
        computeAcmRankNo(removeStar, contest, currentUserId, concernedList, orderResultList, topAcmRankVoList);
        topAcmRankVoList.addAll(orderResultList);
        return topAcmRankVoList;
    }


    /**
     * @param isOpenSealRank 是否是查询封榜后的数据
     * @param removeStar     是否需要移除打星队伍
     * @param contest        比赛实体信息
     * @param currentUserId  当前查看榜单的用户uuid,不为空则将该数据复制一份放置列表最前
     * @param concernedList  关注的用户（uuid）列表
     * @param useCache       是否对初始排序计算的结果进行缓存
     * @param cacheTime      缓存的时间 单位秒
     * @MethodName calcOIRank
     * @Description
     * @Return
     * @Since 2021/12/10
     */
    public List<OIContestRankVo> calculateOiRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                 String currentUserId, List<String> concernedList, boolean useCache, Long cacheTime) {
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
        computeOiRankNo(contest, currentUserId, concernedList, removeStar, orderResultList, topOiRankVoList);
        topOiRankVoList.addAll(orderResultList);
        return topOiRankVoList;
    }

    private List<ACMContestRankVo> getAcmOrderRank(Contest contest, Boolean isOpenSealRank) {
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        List<ContestRecordVo> contestRecordList = contestRecordEntityService.getACMContestRecord(contest.getAuthor(),
                contest.getId())
                .stream()
                .filter(contestRecord -> !contestRecord.getUid().equals(contest.getUid()) && !superAdminUidList.contains(contestRecord.getUid()))
                .collect(Collectors.toList());
        Set<String> hasRecordUserNameSet = contestRecordList.stream()
                .map(ContestRecordVo::getUsername)
                .collect(Collectors.toSet());
        Map<String, ACMContestRankVo> uidContestRankVoMap = initAcmContestRankVo(hasRecordUserNameSet);
        List<ACMContestRankVo> result = new ArrayList<>(uidContestRankVoMap.values());

        HashMap<String, Long> firstAcMap = new HashMap<>();
        for (ContestRecordVo contestRecord : contestRecordList) {
            ACMContestRankVo acmContestRankVo = uidContestRankVoMap.get(contestRecord.getUid());
            HashMap<String, Object> problemSubmissionInfo = acmContestRankVo.getSubmissionInfo()
                    .get(contestRecord.getDisplayId());

            if (problemSubmissionInfo == null) {
                problemSubmissionInfo = new HashMap<>();
                problemSubmissionInfo.put("errorNum", 0);
            }

            acmContestRankVo.setTotal(acmContestRankVo.getTotal() + 1);

            // 如果是当前是开启封榜的时段和同时该提交是处于封榜时段 尝试次数+1
            if (isOpenSealRank && isInSealTimeSubmission(contest, contestRecord.getSubmitTime())) {
                int tryNum = (int) problemSubmissionInfo.getOrDefault("tryNum", 0);
                problemSubmissionInfo.put("tryNum", tryNum + 1);
            } else {
                // 如果该题目已经AC过了，其它都不记录了
                if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                    continue;
                }
                processContestRecordVo(contestRecord, firstAcMap, acmContestRankVo, problemSubmissionInfo);
            }
            acmContestRankVo.getSubmissionInfo().put(contestRecord.getDisplayId(), problemSubmissionInfo);
        }

        // 先以总ac数降序，再以总耗时升序
        result = result.stream()
                .sorted(Comparator.comparing(ACMContestRankVo::getAc, Comparator.reverseOrder())
                        .thenComparing(ACMContestRankVo::getTotalTime))
                .collect(Collectors.toList());

        if (contestValidator.isContestAdmin(contest)) {
            result.addAll(getNoRecordUserAcmContestRankVos(contest, hasRecordUserNameSet));
        }
        return result;
    }

    private List<OIContestRankVo> getOiOrderRank(Contest contest, Boolean isOpenSealRank) {
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        final List<ContestRecordVo> oiContestRecordList = contestRecordEntityService.getOIContestRecord(contest, isOpenSealRank)
                .stream()
                .filter(contestRecord -> !contestRecord.getUid().equals(contest.getUid()) && !superAdminUidList.contains(contestRecord.getUid()))
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

    private void processContestRecordVo(ContestRecordVo contestRecord, HashMap<String, Long> firstAcMap, ACMContestRankVo acmContestRankVo, HashMap<String, Object> problemSubmissionInfo) {
        // 记录已经按题目提交耗时time升序了
        // 通过的话
        if (contestRecord.getStatus().intValue() == ContestEnum.RECORD_AC.getCode()) {
            // 总解决题目次数ac+1
            acmContestRankVo.setAc(acmContestRankVo.getAc() + 1);

            // 判断是不是first AC
            boolean isFirstAc = false;
            Long time = firstAcMap.getOrDefault(contestRecord.getDisplayId(), null);
            if (time == null) {
                isFirstAc = true;
                firstAcMap.put(contestRecord.getDisplayId(), contestRecord.getTime());
            } else {
                // 相同提交时间也是first AC
                if (time.longValue() == contestRecord.getTime().longValue()) {
                    isFirstAc = true;
                }
            }

            int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
            problemSubmissionInfo.put("isAC", true);
            problemSubmissionInfo.put("isFirstAC", isFirstAc);
            problemSubmissionInfo.put("ACTime", contestRecord.getTime());
            problemSubmissionInfo.put("errorNum", errorNumber);

            // 同时计算总耗时，总耗时加上 该题目未AC前的错误次数*20*60+题目AC耗时
            acmContestRankVo.setTotalTime(
                    acmContestRankVo.getTotalTime() + errorNumber * 20 * 60 + contestRecord.getTime());
        } else if (contestRecord.getStatus().intValue() == ContestEnum.RECORD_NOT_AC_PENALTY.getCode()) {
            // 未通过同时需要记录罚时次数
            int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
            problemSubmissionInfo.put("errorNum", errorNumber + 1);
        } else {
            int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
            problemSubmissionInfo.put("errorNum", errorNumber);
        }
    }

    private void computeAcmRankNo(boolean removeStar, Contest contest, String currentUserId, List<String> concernedList, List<ACMContestRankVo> orderResultList, List<ACMContestRankVo> topAcmRankVoList) {
        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
        if (removeStar) {
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
        }

        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            // 移除关注列表与当前用户重复
            concernedList.remove(currentUserId);
        }

        int rankNum = 1;
        ACMContestRankVo preAcmRankVo = null;
        for (int i = 0; i < orderResultList.size(); i++) {
            ACMContestRankVo currentAcmRankVo = orderResultList.get(i);
            currentAcmRankVo.setSeq(i + 1);
            if (starAccountMap.containsKey(currentAcmRankVo.getUsername())) {
                // 打星队伍排名为-1
                currentAcmRankVo.setRank(-1);
            } else {
                if (rankNum == 1) {
                    currentAcmRankVo.setRank(rankNum);
                } else {
                    // 当前用户的总罚时和AC数跟前一个用户一样的话，同时前一个不应该为打星，排名则一样
                    if (preAcmRankVo.getAc().equals(currentAcmRankVo.getAc())
                            && preAcmRankVo.getTotalTime().equals(currentAcmRankVo.getTotalTime())) {
                        currentAcmRankVo.setRank(preAcmRankVo.getRank());
                    } else {
                        currentAcmRankVo.setRank(rankNum);
                    }
                }
                preAcmRankVo = currentAcmRankVo;
                rankNum++;
            }

            if (!StrUtil.isEmpty(currentUserId) && currentAcmRankVo.getUid().equals(currentUserId)) {
                topAcmRankVoList.add(currentAcmRankVo);
            }

            // 需要添加关注用户
            if (needAddConcernedUser) {
                if (concernedList.contains(currentAcmRankVo.getUid())) {
                    topAcmRankVoList.add(currentAcmRankVo);
                }
            }
        }
    }

    private void computeOiRankNo(Contest contest, String currentUserId, List<String> concernedList, boolean removeStar, List<OIContestRankVo> orderResultList, List<OIContestRankVo> topOiRankVoList) {
        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());
        // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
        if (removeStar) {
            orderResultList.removeIf(contestRankVo -> starAccountMap.containsKey(contestRankVo.getUsername()));
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

    private List<UserInfo> getNoRecordUserInfos(Contest contest, Set<String> userNameSet) {
        String extra = ReUtil.getGroup1("<extra>([\\S\\s]*?)<\\/extra>", contest.getAccountLimitRule());
        if(StrUtil.isBlank(extra)){
            return new ArrayList<>();
        }
        final Set<String> extraUserNameSet = Arrays.stream(extra.split("\n"))
                .filter(u -> !userNameSet.contains(u))
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(extraUserNameSet)) {
            return new ArrayList<>();
        }
        return userInfoEntityService.lambdaQuery()
                .in(UserInfo::getUsername, extraUserNameSet)
                .list();
    }

    private List<OIContestRankVo> getNoRecordUserOiContestRankVos(Contest contest, Set<String> hasRecordUserNameSet) {
        final Set<String> registeredUsers = contestRegisterEntityService.getRegisteredUsers(contest.getId());
        final List<UserInfo> noRecordUserInfos = getNoRecordUserInfos(contest, hasRecordUserNameSet);
        return noRecordUserInfos.stream()
                .map(this::initOiContestRankVo)
                .map(contestRankVo -> contestRankVo.setRegistered(registeredUsers.contains(contestRankVo.getUid())))
                .sorted(Comparator.comparing(OIContestRankVo::getRegistered, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private List<ACMContestRankVo> getNoRecordUserAcmContestRankVos(Contest contest, Set<String> userNameSet) {
        final Set<String> registeredUsers = contestRegisterEntityService.getRegisteredUsers(contest.getId());
        final List<UserInfo> noRecordUserInfos = getNoRecordUserInfos(contest, userNameSet);
        return noRecordUserInfos.stream()
                .map(this::initAcmContestRankVo)
                .map(contestRankVo -> contestRankVo.setRegistered(registeredUsers.contains(contestRankVo.getUid())))
                .sorted(Comparator.comparing(ACMContestRankVo::getRegistered, Comparator.reverseOrder()))
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

    private Map<String, ACMContestRankVo> initAcmContestRankVo(Set<String> userNameSet) {
        if (CollUtil.isEmpty(userNameSet)) {
            return new HashMap<>();
        }

        return userInfoEntityService.lambdaQuery()
                .in(UserInfo::getUsername, userNameSet)
                .list()
                .stream()
                .map(this::initAcmContestRankVo)
                .collect(Collectors.toMap(ACMContestRankVo::getUid, o -> o));
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

    private ACMContestRankVo initAcmContestRankVo(UserInfo userInfo) {
        return new ACMContestRankVo()
                .setRealname(userInfo.getRealname())
                .setUid(userInfo.getUuid())
                .setUsername(userInfo.getUsername())
                .setSchool(userInfo.getSchool())
                .setAvatar(userInfo.getAvatar())
                .setGender(userInfo.getGender())
                .setNickname(userInfo.getNickname())
                .setAc(0)
                .setTotalTime(0L)
                .setTotal(0)
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

    private boolean isInSealTimeSubmission(Contest contest, Date submissionDate) {
        return DateUtil.isIn(submissionDate, contest.getSealRankTime(), contest.getEndTime());
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