package com.simplefanc.voj.common.constants;

public interface RedisConstant {
    String OI_CONTEST_RANK_CACHE = "oi_contest_rank_cache";

    String CONTEST_RANK_CAL_RESULT_CACHE = "contest_rank_cal_result_cache";

    String ACM_RANK_CACHE = "acm_rank_cache";

    String OI_RANK_CACHE = "oi_rank_cache";

    String SUPER_ADMIN_UID_LIST_CACHE = "super_admin_uid_list_cache";

    String CODE_CHANGE_PASSWORD_FAIL = "change-password-fail:";

    String CODE_CHANGE_PASSWORD_LOCK = "change-password-lock:";

    String CODE_ACCOUNT_LOCK = "account-lock:";

    String CODE_CHANGE_EMAIL_FAIL = "change-email-fail:";

    String CODE_CHANGE_EMAIL_LOCK = "change-email-lock:";

    String TRY_LOGIN_NUM = "try-login-num:";

    String SUBMIT_NON_CONTEST_LOCK = "submit_non_contest_lock:";

    String SUBMIT_CONTEST_LOCK = "submit_contest_lock:";

    String DISCUSSION_ADD_NUM_LOCK = "discussion_add_num_lock:";

    String CONTEST_ADD_PRINT_LOCK = "contest_add_print_lock:";
}