package com.simplefanc.voj.judger.judge.remote.provider.shared.codeforces;

import com.simplefanc.voj.judger.judge.remote.httpclient.CookieUtil;
import com.simplefanc.voj.judger.judge.remote.httpclient.DedicatedHttpClient;
import lombok.experimental.UtilityClass;

/**
 * @author chenfan
 * @date 2022/7/25 10:24
 **/
@UtilityClass
public class CFUtil {

    public String getTTA(DedicatedHttpClient client) {
        String _39ce7 = CookieUtil.getCookieValue(client, "39ce7");
        int _tta = 0;
        for (int c = 0; c < _39ce7.length(); c++) {
            _tta = (_tta + (c + 1) * (c + 2) * _39ce7.charAt(c)) % 1009;
            if (c % 3 == 0)
                _tta++;
            if (c % 2 == 0)
                _tta *= 2;
            if (c > 0)
                _tta -= (_39ce7.charAt(c / 2) / 2) * (_tta % 5);
            while (_tta < 0)
                _tta += 1009;
            while (_tta >= 1009)
                _tta -= 1009;
        }
        return Integer.toString(_tta);
    }
}
