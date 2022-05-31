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

    public List<ACMContestRankVo> calculateACMRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                   String currentUserId, List<String> concernedList) {
        return calculateACMRank(isOpenSealRank, removeStar, contest, currentUserId, concernedList, false, null);
    }

    public List<OIContestRankVo> calculateOIRank(Boolean isOpenSealRank, Boolean removeStar, Contest contest,
                                                 String currentUserId, List<String> concernedList) {

        return calculateOIRank(isOpenSealRank, removeStar, contest, currentUserId, concernedList, false, null);
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
    public List<ACMContestRankVo> calculateACMRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                   String currentUserId, List<String> concernedList, boolean useCache, Long cacheTime) {

        List<ACMContestRankVo> orderResultList;
        if (useCache) {
            String key = ContestConstant.CONTEST_RANK_CAL_RESULT_CACHE + "_" + contest.getId();
            orderResultList = (List<ACMContestRankVo>) redisUtil.get(key);
            if (orderResultList == null) {
                orderResultList = getACMOrderRank(contest, isOpenSealRank);
                redisUtil.set(key, orderResultList, cacheTime);
            }
        } else {
            orderResultList = getACMOrderRank(contest, isOpenSealRank);
        }

        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
        if (removeStar) {
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
        }
        // 记录当前用户排名数据和关注列表的用户排名数据
        List<ACMContestRankVo> topACMRankVoList = new ArrayList<>();
        boolean needAddConcernedUser = false;
        if (!CollectionUtils.isEmpty(concernedList)) {
            needAddConcernedUser = true;
            // 移除关注列表与当前用户重复
            concernedList.remove(currentUserId);
        }

        int rankNum = 1;
        int len = orderResultList.size();
        ACMContestRankVo lastACMRankVo = null;
        for (int i = 0; i < len; i++) {
            ACMContestRankVo currentACMRankVo = orderResultList.get(i);
            if (starAccountMap.containsKey(currentACMRankVo.getUsername())) {
                // 打星队伍排名为-1
                currentACMRankVo.setRank(-1);
            } else {
                if (rankNum == 1) {
                    currentACMRankVo.setRank(rankNum);
                } else {
                    // 当前用户的总罚时和AC数跟前一个用户一样的话，同时前一个不应该为打星，排名则一样
                    if (lastACMRankVo.getAc().equals(currentACMRankVo.getAc())
                            && lastACMRankVo.getTotalTime().equals(currentACMRankVo.getTotalTime())) {
                        currentACMRankVo.setRank(lastACMRankVo.getRank());
                    } else {
                        currentACMRankVo.setRank(rankNum);
                    }
                }
                lastACMRankVo = currentACMRankVo;
                rankNum++;
            }

            if (!StrUtil.isEmpty(currentUserId) && currentACMRankVo.getUid().equals(currentUserId)) {
                topACMRankVoList.add(currentACMRankVo);
            }

            // 需要添加关注用户
            if (needAddConcernedUser) {
                if (concernedList.contains(currentACMRankVo.getUid())) {
                    topACMRankVoList.add(currentACMRankVo);
                }
            }
        }
        topACMRankVoList.addAll(orderResultList);
        return topACMRankVoList;
    }

    // TODO 行数过多
    private List<ACMContestRankVo> getACMOrderRank(Contest contest, Boolean isOpenSealRank) {

        List<ContestRecordVo> contestRecordList = contestRecordEntityService.getACMContestRecord(contest.getAuthor(),
                contest.getId());

        List<String> superAdminUidList = getSuperAdminUidList();

        List<ACMContestRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        int index = 0;

        HashMap<String, Long> firstACMap = new HashMap<>();

        for (ContestRecordVo contestRecord : contestRecordList) {
            // 超级管理员的提交不入排行榜
            if (superAdminUidList.contains(contestRecord.getUid())) {
                continue;
            }

            ACMContestRankVo ACMContestRankVo;
            // 如果该用户信息没还记录
            if (!uidMapIndex.containsKey(contestRecord.getUid())) {

                // 初始化参数
                ACMContestRankVo = new ACMContestRankVo();
                ACMContestRankVo.setRealname(contestRecord.getRealname())
                        .setAvatar(contestRecord.getAvatar())
                        .setSchool(contestRecord.getSchool())
                        .setGender(contestRecord.getGender())
                        .setUid(contestRecord.getUid())
                        .setUsername(contestRecord.getUsername())
                        .setNickname(contestRecord.getNickname())
                        .setAc(0)
                        .setTotalTime(0L)
                        .setTotal(0);

                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                ACMContestRankVo.setSubmissionInfo(submissionInfo);

                result.add(ACMContestRankVo);
                uidMapIndex.put(contestRecord.getUid(), index);
                index++;
            } else {
                // 根据记录的index进行获取
                ACMContestRankVo = result.get(uidMapIndex.get(contestRecord.getUid()));
            }
            // TODO put 键
            HashMap<String, Object> problemSubmissionInfo = ACMContestRankVo.getSubmissionInfo()
                    .get(contestRecord.getDisplayId());

            if (problemSubmissionInfo == null) {
                problemSubmissionInfo = new HashMap<>();
                problemSubmissionInfo.put("errorNum", 0);
            }

            ACMContestRankVo.setTotal(ACMContestRankVo.getTotal() + 1);

            // 如果是当前是开启封榜的时段和同时该提交是处于封榜时段 尝试次数+1
            if (isOpenSealRank && isInSealTimeSubmission(contest, contestRecord.getSubmitTime())) {

                int tryNum = (int) problemSubmissionInfo.getOrDefault("tryNum", 0);
                problemSubmissionInfo.put("tryNum", tryNum + 1);
            } else {
                // 如果该题目已经AC过了，其它都不记录了
                if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                    continue;
                }

                // 记录已经按题目提交耗时time升序了

                // 通过的话
                if (contestRecord.getStatus().intValue() == ContestEnum.RECORD_AC.getCode()) {
                    // 总解决题目次数ac+1
                    ACMContestRankVo.setAc(ACMContestRankVo.getAc() + 1);

                    // 判断是不是first AC
                    boolean isFirstAC = false;
                    Long time = firstACMap.getOrDefault(contestRecord.getDisplayId(), null);
                    if (time == null) {
                        isFirstAC = true;
                        firstACMap.put(contestRecord.getDisplayId(), contestRecord.getTime());
                    } else {
                        // 相同提交时间也是first AC
                        if (time.longValue() == contestRecord.getTime().longValue()) {
                            isFirstAC = true;
                        }
                    }

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("isAC", true);
                    problemSubmissionInfo.put("isFirstAC", isFirstAC);
                    problemSubmissionInfo.put("ACTime", contestRecord.getTime());
                    problemSubmissionInfo.put("errorNum", errorNumber);

                    // 同时计算总耗时，总耗时加上 该题目未AC前的错误次数*20*60+题目AC耗时
                    ACMContestRankVo.setTotalTime(
                            ACMContestRankVo.getTotalTime() + errorNumber * 20 * 60 + contestRecord.getTime());

                    // 未通过同时需要记录罚时次数
                } else if (contestRecord.getStatus().intValue() == ContestEnum.RECORD_NOT_AC_PENALTY.getCode()) {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber + 1);
                } else {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber);
                }
            }
            ACMContestRankVo.getSubmissionInfo().put(contestRecord.getDisplayId(), problemSubmissionInfo);
        }

        return result.stream()
                // 再以总耗时升序
                // 先以总ac数降序
                .sorted(Comparator.comparing(ACMContestRankVo::getAc, Comparator.reverseOrder())
                        .thenComparing(ACMContestRankVo::getTotalTime))
                .collect(Collectors.toList());
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
    public List<OIContestRankVo> calculateOIRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                 String currentUserId, List<String> concernedList, boolean useCache, Long cacheTime) {
        List<OIContestRankVo> orderResultList;
        if (useCache) {
            String key = ContestConstant.CONTEST_RANK_CAL_RESULT_CACHE + "_" + contest.getId();
            orderResultList = (List<OIContestRankVo>) redisUtil.get(key);
            if (orderResultList == null) {
                orderResultList = getOIOrderRank(contest, isOpenSealRank);
                redisUtil.set(key, orderResultList, cacheTime);
            }
        } else {
            orderResultList = getOIOrderRank(contest, isOpenSealRank);
        }
        // 记录当前用户排名数据和关注列表的用户排名数据
        List<OIContestRankVo> topOIRankVoList = new ArrayList<>();
        // 设置 rank 和 添加置顶
        computeRankNo(contest, currentUserId, concernedList, removeStar, orderResultList, topOIRankVoList);
        topOIRankVoList.addAll(orderResultList);
        return topOIRankVoList;
    }

    private void computeRankNo(Contest contest, String currentUserId, List<String> concernedList, boolean removeStar, List<OIContestRankVo> orderResultList, List<OIContestRankVo> topOIRankVoList) {
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
                topOIRankVoList.add(contestRankVo);
            }

            // 添加关注用户
            if (needAddConcernedUser && concernedList.contains(contestRankVo.getUid())) {
                topOIRankVoList.add(contestRankVo);
            }
        }
    }

    private List<OIContestRankVo> getOIOrderRank(Contest contest, Boolean isOpenSealRank) {
        List<String> superAdminUidList = getSuperAdminUidList();
        final List<ContestRecordVo> oiContestRecordList = contestRecordEntityService.getOIContestRecord(contest, isOpenSealRank)
                .stream()
                .filter(contestRecord -> !contestRecord.getUid().equals(contest.getUid()) && !superAdminUidList.contains(contestRecord.getUid()))
                .collect(Collectors.toList());
        Set<String> hasRecordUserNameSet = oiContestRecordList.stream()
                .map(ContestRecordVo::getUsername)
                .collect(Collectors.toSet());

        Map<String, OIContestRankVo> uidContestRankVoMap = initContestRankVo(hasRecordUserNameSet);

        List<OIContestRankVo> result = new ArrayList<>(uidContestRankVoMap.values());

        boolean isHighestRankScore = ContestConstant.OI_RANK_HIGHEST_SCORE.equals(contest.getOiRankScoreType());
        oiContestRecordList.forEach(contestRecord ->
                setSubmissionInfo(uidContestRankVoMap.get(contestRecord.getUid()), isHighestRankScore, contestRecord));

        HashMap<String, Map<String, Integer>> uidTimeInfoMap = getUidTimeInfoMap(oiContestRecordList);
        setTimeInfoToResult(result, uidTimeInfoMap);

        // 根据总得分进行降序，再根据总时耗升序排序
        result = result.stream()
                .sorted(Comparator.comparing(OIContestRankVo::getTotalScore, Comparator.reverseOrder())
                        .thenComparing(OIContestRankVo::getTotalTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());

        if (contestValidator.isContestAdmin(contest)) {
            result.addAll(getNoRecordUserContestRankVos(contest, hasRecordUserNameSet));
        }
        return result;
    }

    private List<OIContestRankVo> getNoRecordUserContestRankVos(Contest contest, Set<String> userNameSet) {
        String extra = ReUtil.getGroup1("<extra>([\\S\\s]*?)<\\/extra>", contest.getAccountLimitRule());
        final Set<String> extraUserNameSet = Arrays.stream(extra.split("\n"))
                .filter(u -> !userNameSet.contains(u))
                .collect(Collectors.toSet());
        if (CollUtil.isEmpty(extraUserNameSet)) {
            return new ArrayList<>();
        }

        final Set<String> registeredUsers = contestRegisterEntityService.lambdaQuery()
                .select(ContestRegister::getUid)
                .eq(ContestRegister::getCid, contest.getId())
                .list()
                .stream()
                .map(ContestRegister::getUid)
                .collect(Collectors.toSet());

        return userInfoEntityService.lambdaQuery()
                .in(UserInfo::getUsername, extraUserNameSet)
                .list()
                .stream()
                .map(this::initOiContestRankVo)
                .map(contestRankVo -> contestRankVo.setRegistered(registeredUsers.contains(contestRankVo.getUid())))
                .sorted(Comparator.comparing(OIContestRankVo::getRegistered, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    private Map<String, OIContestRankVo> initContestRankVo(Set<String> userNameSet) {
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
    private void setSubmissionInfo(OIContestRankVo oiContestRankVo, boolean isHighestRankScore, ContestRecordVo contestRecord) {
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

    private List<String> getSuperAdminUidList() {
        return userInfoEntityService.getSuperAdminUidList();
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