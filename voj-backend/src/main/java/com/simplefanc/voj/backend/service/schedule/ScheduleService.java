package com.simplefanc.voj.backend.service.schedule;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.utils.RedisUtil;
import com.simplefanc.voj.backend.dao.common.FileEntityService;
import com.simplefanc.voj.backend.dao.judge.JudgeEntityService;
import com.simplefanc.voj.backend.dao.msg.AdminSysNoticeEntityService;
import com.simplefanc.voj.backend.dao.msg.UserSysNoticeEntityService;
import com.simplefanc.voj.backend.dao.user.SessionEntityService;
import com.simplefanc.voj.backend.dao.user.UserInfoEntityService;
import com.simplefanc.voj.backend.dao.user.UserRecordEntityService;
import com.simplefanc.voj.backend.pojo.bo.FilePathProps;
import com.simplefanc.voj.backend.service.admin.rejudge.RejudgeService;
import com.simplefanc.voj.common.constants.JudgeStatus;
import com.simplefanc.voj.common.pojo.entity.common.File;
import com.simplefanc.voj.common.pojo.entity.judge.Judge;
import com.simplefanc.voj.common.pojo.entity.msg.AdminSysNotice;
import com.simplefanc.voj.common.pojo.entity.msg.UserSysNotice;
import com.simplefanc.voj.common.pojo.entity.user.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 一个cron表达式有至少6个（也可能7个）有空格分隔的时间元素。按顺序依次为：
 * <p>
 * 字段 允许值 允许的特殊字符 秒 0~59 , - * / 分 0~59 , - * / 小时 0~23 , - * / 日期 1-31 , - * ? / L W C 月份
 * 1~12或者JAN~DEC , - * / 星期 1~7或者SUN~SAT , - * ? / L C # 年（可选） 留空，1970~2099 , - * /
 * <p>
 * “*” 字符代表所有可能的值 “-” 字符代表数字范围 例如1-5 “/” 字符用来指定数值的增量 “？” 字符仅被用于天（月）和天（星期）两个子表达式，表示不指定值。
 * 当2个子表达式其中之一被指定了值以后，为了避免冲突，需要将另一个子表达式的值设为“？” “L” 字符仅被用于天（月）和天（星期）两个子表达式，它是单词“last”的缩写
 * 如果在“L”前有具体的内容，它就具有其他的含义了。 “W” 字符代表着平日(Mon-Fri)，并且仅能用于日域中。它用来指定离指定日的最近的一个平日。
 * 大部分的商业处理都是基于工作周的，所以 W 字符可能是非常重要的。 "C"
 * 代表“Calendar”的意思。它的意思是计划所关联的日期，如果日期没有被关联，则相当于日历中所有日期。
 */
@Service
@Slf4j(topic = "voj")
@RequiredArgsConstructor
public class ScheduleService {

    private final FileEntityService fileEntityService;

    private final RedisUtil redisUtil;

    private final UserInfoEntityService userInfoEntityService;

    private final UserRecordEntityService userRecordEntityService;

    private final SessionEntityService sessionEntityService;

    private final AdminSysNoticeEntityService adminSysNoticeEntityService;

    private final UserSysNoticeEntityService userSysNoticeEntityService;

    private final JudgeEntityService judgeEntityService;

    private final RejudgeService rejudgeService;

    private final FilePathProps filePathProps;

    /**
     * @MethodName deleteAvatar
     * @Params * @param null
     * @Description 每天3点定时查询数据库字段并删除未引用的头像
     * @Return
     * @Since 2021/1/13
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteAvatar() {
        List<File> files = fileEntityService.queryDeleteAvatarList();
        // 如果查不到，直接结束
        if (files.isEmpty()) {
            return;
        }
        List<Long> idLists = new LinkedList<>();
        for (File file : files) {
            if (file.getDelete()) {
                boolean delSuccess = FileUtil.del(file.getFilePath());
                if (delSuccess) {
                    idLists.add(file.getId());
                }
            }
        }

        boolean isSuccess = fileEntityService.removeByIds(idLists);
        if (!isSuccess) {
            log.error("数据库file表删除头像数据失败----------------->sql语句执行失败");
        }
    }

    /**
     * @MethodName deleteTestCase
     * @Params * @param null
     * @Description 每天3点定时删除指定文件夹的上传测试数据
     * @Return
     * @Since 2021/2/7
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteTestCase() {
        boolean result = FileUtil.del(filePathProps.getTestcaseTmpFolder());
        if (!result) {
            log.error("每日定时任务异常------------------------>{}", "清除本地的题目测试数据失败!");
        }
    }

    /**
     * @MethodName deleteContestPrintText
     * @Params * @param null
     * @Description 每天4点定时删除本地的比赛打印数据
     * @Return
     * @Since 2021/9/19
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void deleteContestPrintText() {
        boolean result = FileUtil.del(filePathProps.getContestTextPrintFolder());
        if (!result) {
            log.error("每日定时任务异常------------------------>{}", "清除本地的比赛打印数据失败!");
        }
    }

    /**
     * @MethodName deleteUserSession
     * @Params * @param null
     * @Description 每天3点定时删除用户半年的session表记录
     * @Return
     * @Since 2021/9/6
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteUserSession() {
        QueryWrapper<Session> sessionQueryWrapper = new QueryWrapper<>();
        DateTime dateTime = DateUtil.offsetMonth(new Date(), -6);
        String strTime = dateTime.toString(DatePattern.NORM_DATETIME_FORMAT);
        sessionQueryWrapper.select("distinct uid");
        sessionQueryWrapper.apply("UNIX_TIMESTAMP(gmt_create) >= UNIX_TIMESTAMP('" + strTime + "')");
        List<Session> sessionList = sessionEntityService.list(sessionQueryWrapper);
        if (sessionList.size() > 0) {
            List<String> uidList = sessionList.stream().map(Session::getUid).collect(Collectors.toList());
            QueryWrapper<Session> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("uid", uidList).apply("UNIX_TIMESTAMP('" + strTime + "') > UNIX_TIMESTAMP(gmt_create)");
            List<Session> needDeletedSessionList = sessionEntityService.list(queryWrapper);
            if (needDeletedSessionList.size() > 0) {
                List<Long> needDeletedIdList = needDeletedSessionList.stream().map(Session::getId)
                        .collect(Collectors.toList());
                boolean isOk = sessionEntityService.removeByIds(needDeletedIdList);
                if (!isOk) {
                    log.error("=============数据库session表定时删除用户6个月前的记录失败===============");
                }
            }
        }
    }

    /**
     * @MethodName syncNoticeToUser
     * @Description 每一小时拉取系统通知表admin_sys_notice到表user_sys_notice(只推送给半年内有登录过的用户)
     * @Return
     * @Since 2021/10/3
     */
    @Scheduled(cron = "0 0 0/1 * * *")
    public void syncNoticeToRecentHalfYearUser() {
        QueryWrapper<AdminSysNotice> adminSysNoticeQueryWrapper = new QueryWrapper<>();
        adminSysNoticeQueryWrapper.eq("state", false);
        List<AdminSysNotice> adminSysNotices = adminSysNoticeEntityService.list(adminSysNoticeQueryWrapper);
        if (adminSysNotices.size() == 0) {
            return;
        }

        QueryWrapper<Session> sessionQueryWrapper = new QueryWrapper<>();
        sessionQueryWrapper.select("DISTINCT uid");
        List<Session> sessionList = sessionEntityService.list(sessionQueryWrapper);
        List<String> userIds = sessionList.stream().map(Session::getUid).collect(Collectors.toList());

        for (AdminSysNotice adminSysNotice : adminSysNotices) {
            switch (adminSysNotice.getType()) {
                // TODO 魔法
                case "All":
                    List<UserSysNotice> userSysNoticeList = new ArrayList<>();
                    for (String uid : userIds) {
                        UserSysNotice userSysNotice = new UserSysNotice();
                        userSysNotice.setRecipientId(uid).setType("Sys").setSysNoticeId(adminSysNotice.getId());
                        userSysNoticeList.add(userSysNotice);
                    }
                    boolean isOk1 = userSysNoticeEntityService.saveOrUpdateBatch(userSysNoticeList);
                    if (isOk1) {
                        adminSysNotice.setState(true);
                    }
                    break;
                case "Single":
                    UserSysNotice userSysNotice = new UserSysNotice();
                    userSysNotice.setRecipientId(adminSysNotice.getRecipientId()).setType("Mine")
                            .setSysNoticeId(adminSysNotice.getId());
                    boolean isOk2 = userSysNoticeEntityService.saveOrUpdate(userSysNotice);
                    if (isOk2) {
                        adminSysNotice.setState(true);
                    }
                    break;
                case "Admin":
                    break;
            }

        }

        boolean isUpdateNoticeOk = adminSysNoticeEntityService.saveOrUpdateBatch(adminSysNotices);
        if (!isUpdateNoticeOk) {
            log.error("=============推送系统通知更新状态失败===============");
        }

    }

    @Scheduled(cron = "0 0/20 * * * ?")
    public void check20MPendingSubmission() {
        DateTime dateTime = DateUtil.offsetMinute(new Date(), -15);
        String strTime = dateTime.toString(DatePattern.NORM_DATETIME_FORMAT);

        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("distinct submit_id");
        judgeQueryWrapper.eq("status", JudgeStatus.STATUS_PENDING.getStatus());
        judgeQueryWrapper.apply("UNIX_TIMESTAMP('" + strTime + "') > UNIX_TIMESTAMP(gmt_modified)");
        List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);
        if (!CollectionUtils.isEmpty(judgeList)) {
            log.info("Half An Hour Check Pending Submission to Rejudge:" + Arrays.toString(judgeList.toArray()));
            for (Judge judge : judgeList) {
                rejudgeService.rejudge(judge.getSubmitId());
            }
        }
    }

}
