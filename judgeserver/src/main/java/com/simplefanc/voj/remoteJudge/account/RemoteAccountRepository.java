package com.simplefanc.voj.remoteJudge.account;

import com.simplefanc.voj.pojo.RemoteOj;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RemoteAccountRepository {

    /**
     * accountId -> RemoteAccount
     */
    private Map<String, RemoteAccount> repo = new HashMap<>();

    public RemoteAccount getRemoteAccount(RemoteOj remoteOj, String username, String password) {
        final String key = remoteOj.getName() + "-" + username;
        if (!repo.containsKey(key)) {
            repo.put(key, new RemoteAccount(remoteOj, username, password));
        }
        return repo.get(key);
    }
}
