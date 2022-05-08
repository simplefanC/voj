package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RemoteOj {

    HDU("HDU"),

    CF("CF"),

    POJ("POJ"),

    JSK("JSK"),

    MXT("MXT"),

    TKOJ("TKOJ"),

    EOJ("EOJ"),
    ;

    private final String name;

    public static RemoteOj getTypeByName(String judgeName) {
        if (judgeName == null)
            return null;
        for (RemoteOj remoteJudge : RemoteOj.values()) {
            if (remoteJudge.getName().equals(judgeName)) {
                return remoteJudge;
            }
        }
        return null;
    }

    public static Boolean isRemoteOJ(String name) {
        for (RemoteOj remoteOJ : RemoteOj.values()) {
            if (remoteOJ.getName().equals(name)) {
                return true;
            }
        }
        return false;

    }
}
