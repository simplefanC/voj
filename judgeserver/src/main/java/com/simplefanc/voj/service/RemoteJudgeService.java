package com.simplefanc.voj.service;

public interface RemoteJudgeService {

    public void changeAccountStatus(String remoteJudge, String username);

    public void changeServerSubmitCFStatus(String ip, Integer port);
}
