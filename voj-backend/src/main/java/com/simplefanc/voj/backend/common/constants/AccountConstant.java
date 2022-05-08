package com.simplefanc.voj.backend.common.constants;

/**
 * @Description 账户相关常量
 * @Since 2021/1/8
 */
public interface AccountConstant {

    String CODE_CHANGE_PASSWORD_FAIL = "change-password-fail:";

    String CODE_CHANGE_PASSWORD_LOCK = "change-password-lock:";

    String CODE_ACCOUNT_LOCK = "account-lock:";

    String CODE_CHANGE_EMAIL_FAIL = "change-email-fail:";

    String CODE_CHANGE_EMAIL_LOCK = "change-email-lock:";

    String TRY_LOGIN_NUM = "try-login-num:";

    String ACM_RANK_CACHE = "acm_rank_cache";

    String OI_RANK_CACHE = "oi_rank_cache";

    String SUPER_ADMIN_UID_LIST_CACHE = "super_admin_uid_list_case";

    String SUBMIT_NON_CONTEST_LOCK = "submit_non_contest_lock:";

    String SUBMIT_CONTEST_LOCK = "submit_contest_lock:";

    String DISCUSSION_ADD_NUM_LOCK = "discussion_add_num_lock:";

    String CONTEST_ADD_PRINT_LOCK = "contest_add_print_lock:";

}
