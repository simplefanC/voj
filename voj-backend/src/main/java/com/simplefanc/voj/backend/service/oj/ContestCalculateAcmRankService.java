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
public class ContestCalculateAcmRankService {

    private final UserInfoEntityService userInfoEntityService;

    private final RedisUtil redisUtil;

    private final ContestRecordEntityService contestRecordEntityService;

    private final ContestRegisterEntityService contestRegisterEntityService;

    private final ContestValidator contestValidator;

    public List<ACMContestRankVo> calculateAcmRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                   List<String> concernedList, String keyword) {
        return calculateAcmRank(isOpenSealRank, removeStar, contest, concernedList, keyword, false, null);
    }

    /**
     * @param isOpenSealRank 是否是查询封榜后的数据
     * @param removeStar     是否需要移除打星队伍
     * @param contest        比赛实体信息
     * @param concernedList  关注的用户（uuid）列表
     * @param useCache       是否对初始排序计算的结果进行缓存
     * @param cacheTime      缓存的时间 单位秒
     * @MethodName calcACMRank
     * @Description
     * @Return
     * @Since 2021/12/10
     */
    public List<ACMContestRankVo> calculateAcmRank(boolean isOpenSealRank, boolean removeStar, Contest contest,
                                                   List<String> concernedList, String keyword, boolean useCache, Long cacheTime) {
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
        computeAcmRankNo(removeStar, contest, concernedList, orderResultList, topAcmRankVoList);
        topAcmRankVoList.addAll(orderResultList);
        if(!StrUtil.isEmpty(keyword)) {
            return topAcmRankVoList.stream()
                    .filter(rankVo -> filterKeyword(rankVo, keyword))
                    .collect(Collectors.toList());
        }
        return topAcmRankVoList;
    }

    boolean filterKeyword(ACMContestRankVo rankVo, String keyword){
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

    /**
     * ACM机制的比赛排名规则：先按AC的题目数量排名，若AC的题目数量一样，则按罚时排名。
     * @param contest
     * @param isOpenSealRank
     * @return
     */
    private List<ACMContestRankVo> getAcmOrderRank(Contest contest, Boolean isOpenSealRank) {
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        List<String> contestAdminUidList = new ArrayList<>(superAdminUidList);
        contestAdminUidList.add(contest.getUid());

        List<ContestRecordVo> contestRecordList = contestRecordEntityService.getACMContestRecord(contest.getId(), contest.getStartTime())
                .stream()
                .filter(contestRecord ->
                        contest.getContestAdminRank() || !contestAdminUidList.contains(contestRecord.getUid())
                )
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

    private void processContestRecordVo(ContestRecordVo contestRecord, HashMap<String, Long> firstAcMap, ACMContestRankVo acmContestRankVo, HashMap<String, Object> problemSubmissionInfo) {
        int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
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

            problemSubmissionInfo.put("isAC", true);
            problemSubmissionInfo.put("isFirstAC", isFirstAc);
            problemSubmissionInfo.put("ACTime", contestRecord.getTime());
            problemSubmissionInfo.put("errorNum", errorNumber);

            // 所谓“罚时”指的是做出题目所用的总时间，加上提交错误所付出的代价，每提交错误一次，会罚时20分钟。
            // 同时计算总耗时，总耗时加上 题目AC耗时+该题目未AC前的错误次数*20*60
            acmContestRankVo.setTotalTime(
                    acmContestRankVo.getTotalTime() + errorNumber * 20 * 60 + contestRecord.getTime());
        } else if (contestRecord.getStatus().intValue() == ContestEnum.RECORD_NOT_AC_PENALTY.getCode()) {
            // 未通过同时需要记录罚时次数
            problemSubmissionInfo.put("errorNum", errorNumber + 1);
        } else {
            problemSubmissionInfo.put("errorNum", errorNumber);
        }
    }

    private void computeAcmRankNo(boolean removeStar, Contest contest, List<String> concernedList, List<ACMContestRankVo> orderResultList, List<ACMContestRankVo> topAcmRankVoList) {
        // 需要打星的用户名列表
        HashMap<String, Boolean> starAccountMap = starAccountToMap(contest.getStarAccount());

        // 如果选择了移除打星队伍，同时该用户属于打星队伍，则将其移除
        if (removeStar) {
            orderResultList.removeIf(acmContestRankVo -> starAccountMap.containsKey(acmContestRankVo.getUsername()));
        }
        final String currentUserId = UserSessionUtil.getUserInfo().getUid();
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

    private List<ACMContestRankVo> getNoRecordUserAcmContestRankVos(Contest contest, Set<String> userNameSet) {
        final Set<String> registeredUsers = contestRegisterEntityService.getRegisteredUsers(contest.getId());
        final List<UserInfo> noRecordUserInfos = getNoRecordUserInfos(contest, userNameSet);
        return noRecordUserInfos.stream()
                .map(this::initAcmContestRankVo)
                .map(contestRankVo -> contestRankVo.setRegistered(registeredUsers.contains(contestRankVo.getUid())))
                .sorted(Comparator.comparing(ACMContestRankVo::getRegistered, Comparator.reverseOrder()))
                .collect(Collectors.toList());
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