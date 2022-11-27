package com.simplefanc.voj.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RemoteOj {

    HDU("HDU"),

    CF("CF"),

    GYM("GYM"),

    AtCoder("AC"),

    POJ("POJ"),

    JSK("JSK"),

    MXT("MXT"),

    TKOJ("TKOJ"),

    EOJ("EOJ"),

    LOJ("LOJ"),
    ;

    private final String name;

    public static RemoteOj getTypeByName(String judgeName) {
        if (judgeName == null) {
            return null;
        }
        for (RemoteOj remoteJudge : RemoteOj.values()) {
            if (remoteJudge.getName().equals(judgeName)) {
                return remoteJudge;
            }
        }
        return null;
    }

    public static Boolean isRemoteOj(String name) {
        for (RemoteOj remoteOj : RemoteOj.values()) {
            if (remoteOj.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
