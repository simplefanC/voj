package com.simplefanc.voj.judger.judge.remote;

import com.simplefanc.voj.common.constants.RemoteOj;
import lombok.Builder;

import java.util.Date;

/**
 * info.remoteOj = RemoteOj.POJ; info.remotePid = "1000"; info.language = "4";
 * info.userCode = "#include <iostream>\n" + "using namespace std;\n" + "int main()\n" +
 * "{\n" + " int a,b;\n" + " cin >> a >> b;\n" + " cout << a+b << endl;\n" + " return
 * 0;\n" + "}";
 */
@Builder
public class SubmissionInfo {

    public Long submitId;

    public String uid;

    public Long cid;

    public Long pid;

    public String serverIp;

    public Integer serverPort;

    public RemoteOj remoteOj;

    /**
     * 如CF的1540C1
     */
    public String remotePid;

    public String remoteContestId;

    public String remoteProblemIndex;

    public String language;

    public String userCode;

    /**
     * leave null if any public remote account is eligible. After submitting, it will set
     * to the defacto account id. 提交后，它将设置为实际帐户id。
     */
    public String remoteAccountId;

    public String remoteRunId;

    public Date remoteSubmitTime;

}
